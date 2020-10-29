package cs203t10.ryver.market.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.exception.TradeNotFoundException;
import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.maker.MarketMaker;
import cs203t10.ryver.market.portfolio.PortfolioService;
import cs203t10.ryver.market.portfolio.asset.InsufficientStockQuantityException;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.util.DateUtils;

@Component
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TradeRepository tradeRepo;

    @Autowired
    private MarketMaker marketMaker;

    /**
    *  Whenever a user submits a trade, the trade is saved and added to the market
    *
    *  Firstly, its a Buy or Sell trade and registers the trade accordingly
    *  If the market is open, trade is added and the market reconciles all trades
    *  Else, trades are just added to the market with No reconcile
    */
    @Override
    public Trade saveTrade(TradeView tradeView) {
        // Set date for new trade
        tradeView.setSubmittedDate(DateUtils.getCurrentDate());

        // Register the trade against the FTS and ensure the trade is valid.
        if (tradeView.getAction() == Action.BUY) {
            registerBuyTrade(tradeView);
        } else {
            registerSellTrade(tradeView);
        }

        if (DateUtils.isMarketOpen(tradeView.getSubmittedDate())) {
            return addTradeToOpenMarket(tradeView);
        }
        return addTradeToClosedMarket(tradeView);
    }

    /**
    *  Add trades when market is open and market reconcile all trades
    */
    private Trade addTradeToOpenMarket(TradeView tradeView) {
        if (tradeView.getAction() == Action.SELL) {
            // Sell trades will increase the trade quantity of the stock records only when the market is open.
            // If the market is closed, the quantity only increases after the market is opened.
            stockRecordService.updateStockRecordAddToMarket(tradeView.getSymbol(), tradeView.getQuantity());
        }

        // By default, trade will be set to OPEN status.
        tradeView.setStatus(Status.OPEN);

        // Save trade.
        Trade trade = tradeView.toTrade();
        Trade toReturn = tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        reconcileMarket(tradeView.getSymbol());

        return toReturn;
    }

    /**
    *  Add trades to the market when it is closed
    */
    private Trade addTradeToClosedMarket(TradeView tradeView) {
        // By default, trade will be set to OPEN status.
        tradeView.setStatus(Status.OPEN);

        // Save trade.
        Trade trade = tradeView.toTrade();
        Trade toReturn = tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        return toReturn;
    }

    /**
     * Reconcile the market by searching for matching buy and sell trades.
     *
     * If market trades are matched, then they will be completed on the fund
     * transfer service, then remove the trade quantity from the stock records.
     */
    @Override
    public void reconcileMarket(String symbol) {
        Trade bestSell = getBestSell(symbol);
        Trade bestBuy = getBestBuy(symbol);

        // The market undergoes reconciliation as long as there is a bestSell
        // and bestBuy.
        while (bestSell != null && bestBuy != null) {
            // Determine transactedPrice.
            Double transactedPrice = 0.0;
            if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0) {
                // Get last price if there are no prices available.
                StockRecord latestStock
                    = stockRecordService.getLatestStockRecordBySymbol(symbol);
                transactedPrice = latestStock.getPrice();
            } else if (bestSell.getPrice() == 0) {
                transactedPrice = bestBuy.getPrice();
            } else if (bestBuy.getPrice() == 0 || bestBuy.getPrice() > bestSell.getPrice()) {
                transactedPrice = bestSell.getPrice();
            } else if (bestBuy.getPrice() < bestSell.getPrice()) {
                return;
            }

            // Determine transactedQuantity.
            Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
            Integer buyQuantity = bestBuy.getQuantity() - bestBuy.getFilledQuantity();
            Integer transactedQuantity = buyQuantity;
            if (sellQuantity < buyQuantity) {
                transactedQuantity = sellQuantity;
            }

            // Update filledQuantity and totalPrice for trades.
            Double totalPrice = transactedQuantity * transactedPrice;
            bestBuy.setFilledQuantity(bestBuy.getFilledQuantity() + transactedQuantity);
            bestSell.setFilledQuantity(bestSell.getFilledQuantity() + transactedQuantity);
            bestBuy.setTotalPrice(bestBuy.getTotalPrice() + totalPrice);
            bestSell.setTotalPrice(bestSell.getTotalPrice() + totalPrice);

            // Update trade.
            updateTrade(bestSell);
            updateTrade(bestBuy);

            // Deduct and add actual balance accordingly.
            completeSellTrade(bestSell, totalPrice);
            completeBuyTrade(bestBuy, totalPrice);

            // Make stock records.
            // Transacted quantity is recorded as negative, as these stocks are leaving the market.
            // Total volume of stocks in stock records will decrease.
            stockRecordService.updateStockRecordRemoveFromMarket(symbol, transactedPrice, transactedQuantity);

            // Get new bestSell and bestBuy.
            bestSell = getBestSell(symbol);
            bestBuy = getBestBuy(symbol);
        }
    }

    /**
     * Register a buy trade on the fund transfer service by deducting the
     * available balance of the appropriate user.
     *
     * The market maker does not need to register trades.
     *
     * The market maker has customerId = 0 and accountId = 0.
     */
    private void registerBuyTrade(TradeView tradeView) {
        Integer customerId = tradeView.getCustomerId();
        Integer accountId = tradeView.getAccountId();
        if (customerId == 0 && accountId == 0) {
            return;
        }

        fundTransferService.deductAvailableBalance(
                customerId, accountId,
                tradeView.getBid() * tradeView.getQuantity()
        );
    }

    /**
     * Register a sell trade on the fund transfer service by deducting the
     * appropriate stocks from the user's portfolio.
     *
     * The market maker does not need to register trades.
     *
     * The market maker has customerId = 0 and accountId = 0.
     */
    private void registerSellTrade(TradeView tradeView) {
        Integer customerId = tradeView.getCustomerId();
        Integer accountId = tradeView.getAccountId();
        String symbol = tradeView.getSymbol();
        Integer quantity = tradeView.getQuantity();
        if (customerId == 0 && accountId == 0) {
            return;
        }

        // Deducts $0 from the account available balance to check if account belongs to customer.
        fundTransferService.deductAvailableBalance(customerId, accountId, 0.0);

        // Check if account has enough stocks from portfolio.
        Integer assetQuantityOwned = portfolioService.getQuantityOfAsset(customerId, symbol);
        if (quantity > assetQuantityOwned) {
            throw new InsufficientStockQuantityException(customerId, symbol);
        }
    }

    /**
    *  Deduct and add actual balance of a buy trade accordingly.
    */
    private void completeBuyTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        // Ignore trades made by market maker.
        if (customerId == 0 && accountId == 0) {
            return;
        }

        // Add stocks to buyer portfolio
        portfolioService.processBuyTrade(trade);

        // Deduct balance from buyer's account.
        fundTransferService.deductBalance(customerId, accountId, totalPrice);
    }

    /**
    *  Deduct and add actual balance of a sell trade accordingly.
    */
    private void completeSellTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        // Ignore trades made by market maker.
        if (customerId == 0 && accountId == 0) {
            return;
        }

        // Deduct stocks from seller portfolio.
        portfolioService.processSellTrade(trade);

        // Add balance to seller's account.
        fundTransferService.addBalance(customerId, accountId, totalPrice);
        fundTransferService.addAvailableBalance(customerId, accountId, totalPrice);
    }

    /**
    *  Retrieve trade using Id
    */
    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId)
                    .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    /**
    *  Retrieve best buy from the market
    */
    @Override
    public Trade getBestBuy(String symbol) {
        Trade bestSell = getBestLimitSellBySymbol(symbol);
        Trade bestMarket = getBestMarketBuyBySymbol(symbol);
        Trade bestLimit = getBestLimitBuyBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) {
            return null;
        }
        if (bestLimit == null) {
            return bestMarket;
        }
        if (bestMarket == null) {
            return bestLimit;
        }

        if (bestSell == null) {
            return bestMarket.getSubmittedDate().before(bestLimit.getSubmittedDate())
                ? bestMarket : bestLimit;
        }

        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        if (bestLimit.getPrice() >= bestSell.getPrice()) {
            return bestLimit;
        } else {
            return bestMarket;
        }
    }

    /**
    *  Retrieve best sell from the market
    */
    @Override
    public Trade getBestSell(String symbol) {
        Trade bestBuy = getBestLimitBuyBySymbol(symbol);
        Trade bestMarket = getBestMarketSellBySymbol(symbol);
        Trade bestLimit = getBestLimitSellBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) {
            return null;
        }
        if (bestLimit == null) {
            return bestMarket;
        }
        if (bestMarket == null) {
            return bestLimit;
        }

        if (bestBuy == null) {
            return bestMarket.getSubmittedDate().before(bestLimit.getSubmittedDate())
                ? bestMarket : bestLimit;
        }
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (bestLimit.getPrice() <= bestBuy.getPrice()) {
            return bestLimit;
        } else {
            return bestMarket;
        }
    }

    private Trade getBestMarketBuyBySymbol(String symbol) {
        return tradeRepo.findBestMarketBuyBySymbol(symbol).orElse(null);
    }

    private Trade getBestMarketSellBySymbol(String symbol) {
        return tradeRepo.findBestMarketSellBySymbol(symbol).orElse(null);
    }

    private Trade getBestLimitBuyBySymbol(String symbol) {
        return tradeRepo.findBestLimitBuyBySymbol(symbol).orElse(null);
    }

    private Trade getBestLimitSellBySymbol(String symbol) {
        return tradeRepo.findBestLimitSellBySymbol(symbol).orElse(null);
    }

    @Override
    public List<Trade> getAllUserOpenTrades(Long customerId) {
        return tradeRepo.findAllByCustomerId(customerId);
    }

    /**
    *  Update a trade's status, price, filled quantity
    */
    @Override
    public Trade updateTrade(Trade newTrade) {
        Integer tradeId = newTrade.getId();
        return tradeRepo.findById(tradeId).map(trade -> {
            trade.setPrice(newTrade.getPrice());
            trade.setFilledQuantity(newTrade.getFilledQuantity());

            // Set status
            if (trade.getFilledQuantity().equals(trade.getQuantity())) {
                trade.setStatus(Status.FILLED);
            } else if (trade.getFilledQuantity() > 0) {
                trade.setStatus(Status.PARTIAL_FILLED);
            } else {
                trade.setStatus(Status.OPEN);
            }
            return tradeRepo.save(trade);
        }).orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    /**
    *  Cancel trade by Id
    */
    @Override
    public Trade cancelTrade(Integer tradeId) {
        Trade trade = getTrade(tradeId);
        if (trade == null) {
            throw new TradeNotFoundException(tradeId);
        }
        trade.setStatus(Status.CANCELLED);
        return tradeRepo.save(trade);
    }

    /**
    *  Save a market trade
    *
    *  Adds market trade into the market and reconciles the trade
    */
    @Override
    public Trade saveMarketMakerTrade(TradeView tradeView) {
        // By default, trade will be set to OPEN status.
        tradeView.setStatus(Status.OPEN);

        // Save trade.
        Trade trade = tradeView.toTrade();
        tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        reconcileMarket(tradeView.getSymbol());

        return getTrade(trade.getId());
    }

    @Override
    public List<Trade> getAllSellTradesBySymbol(String symbol) {
        return tradeRepo.findAllSellTradesBySymbol(symbol);
    }

    @Override
    public List<Trade> getAllBuyTradesBySymbol(String symbol) {
        return tradeRepo.findAllBuyTradesBySymbol(symbol);
    }

    /**
    *  Retrieve total bid volume baserd on stock id
    *
    *  Difference between total buy quantity and filled buy quantity
    *  of valid buy trades (open / partially-filled)
    */
    @Override
    public Integer getTotalBidVolume(String symbol) {
        List<Trade> buyTrades = getAllBuyTradesBySymbol(symbol);
        Integer totalBuyFilledQuantity = 0;
        Integer totalBuyQuantity = 0;
        for (Trade trade : buyTrades) {
            if (checkValidStatus(trade)){
                totalBuyQuantity += trade.getQuantity();
                totalBuyFilledQuantity += trade.getFilledQuantity();
            }
        }
        return totalBuyQuantity - totalBuyFilledQuantity;
    }

    /**
    *  Retrieve total ask volume baserd on stock id
    *
    *  Difference between total sold quantity and total sold filled quantity
    *  of all sell trades (open / partially-filled)
    */
    @Override
    public Integer getTotalAskVolume(String symbol) {
        List<Trade> sellTrades = getAllSellTradesBySymbol(symbol);
        Integer totalSellFilledQuantity = 0;
        Integer totalSellQuantity = 0;
        for (Trade trade : sellTrades) {
            if (checkValidStatus(trade)){
                totalSellQuantity += trade.getQuantity();
                totalSellFilledQuantity += trade.getFilledQuantity();
            }
        }
        return totalSellQuantity - totalSellFilledQuantity;
    }

    /**
    *  Check for valid trades (open / partially-filled)
    */
    public boolean checkValidStatus(Trade trade){
        if (trade.getStatus().equals(Status.PARTIAL_FILLED)
            ||trade.getStatus().equals(Status.OPEN)){
                return true;
            }
        return false;
    }

    /**
    *  Reset market trades
    */
    @Override
    public void resetTrades() {
        tradeRepo.deleteAll();
        portfolioService.resetPortfolios();
        marketMaker.makeNewTrades();
    }

}

