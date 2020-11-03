package cs203t10.ryver.market.trade;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.exception.TradeNotFoundException;
import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.maker.MarketMaker;
import cs203t10.ryver.market.portfolio.PortfolioService;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.portfolio.asset.InsufficientStockQuantityException;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.stock.scrape.FakeScrapingService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeViewCreatable;
import cs203t10.ryver.market.util.DateService;
import cs203t10.ryver.market.util.DoubleUtils;

@Component
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private AssetService assetService;

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

    @Autowired
    private FakeScrapingService fakeScrapingService;

    @Autowired
    private DateService dateService;



    /**
    *  Whenever a user submits a trade, the trade is saved and added to the market
    *
    *  Firstly, its a Buy or Sell trade and registers the trade accordingly
    *  If the market is open, trade is added and the market reconciles all trades
    *  Else, trades are just added to the market with No reconcile
    */
    @Override
    public Trade saveTrade(TradeViewCreatable tradeView) {
        // Set date for new trade
        tradeView.setSubmittedDate(dateService.getCurrentDate());

        // Register the trade against the FTS and ensure the trade is valid.
        Double availableBalance = 0.0;
        if (tradeView.getAction() == Action.BUY) {
            // Note that available balance will only be set for market buys
            // Limit buys, market sells and limit sells available balance
            // will be set to 0.
            availableBalance = registerBuyTrade(tradeView);
        } else {
            registerSellTrade(tradeView);
        }

        if (dateService.isMarketOpen(tradeView.getSubmittedDate())) {
            return addTradeToOpenMarket(tradeView, availableBalance);
        }
        return addTradeToClosedMarket(tradeView, availableBalance);
    }

    /**
    *  Add trades when market is open and market reconcile all trades
    */
    @Override
    public Trade addTradeToOpenMarket(TradeViewCreatable tradeView, Double availableBalance) {
        if (tradeView.getAction() == Action.SELL) {
            // Sell trades will increase the trade quantity of the stock
            // records only when the market is open.
            // If the market is closed, the quantity only increases after
            // the market is opened.
            stockRecordService
                .updateStockRecordAddToMarket(tradeView.getSymbol(), tradeView.getQuantity());
        }

        // Save trade.
        Trade trade = tradeView.toTrade();
        trade.setAvailableBalance(availableBalance);

        Trade toReturn = tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        reconcileMarket(tradeView.getSymbol());

        return toReturn;
    }

    /**
    *  Add trades to the market when it is closed
    */
    private Trade addTradeToClosedMarket(TradeViewCreatable tradeView, Double availableBalance) {
        // Save trade.
        Trade trade = tradeView.toTrade();
        trade.setStatus(Status.CLOSED);
        trade.setAvailableBalance(availableBalance);
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
    @Transactional
    public void reconcileMarket(String symbol) {
        Trade bestSell = getBestSell(symbol);
        Trade bestBuy = getBestBuy(symbol);
        // The market undergoes reconciliation as long as there is a bestSell
        // and bestBuy.
        while (bestSell != null && bestBuy != null) {
            // Determine transactedPrice.
            Double transactedPrice = 0.0;
            Boolean isMarketBuy = bestBuy.getPrice() == 0;
            if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0) {
                transactedPrice = determineTransactedPriceIfBothMarketOrders(symbol, bestSell, bestBuy);
            } else if (bestSell.getPrice() == 0) {
                transactedPrice = bestBuy.getPrice();
            } else if (bestBuy.getPrice() == 0) {
                transactedPrice = bestSell.getPrice();
            } else if (bestBuy.getPrice() >= bestSell.getPrice()){
                transactedPrice = determineTransactedPriceIfBothLimitOrders(bestSell, bestBuy);
            } else if (bestBuy.getPrice() < bestSell.getPrice()) {
                break;
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
            // If market buy, check if available balance is sufficient
            if (isMarketBuy
                && totalPrice + bestBuy.getTotalPrice() > bestBuy.getAvailableBalance() ){
                    transactedQuantity = DoubleUtils
                        .getRoundedToNearestHundred((bestBuy.getAvailableBalance()-bestBuy.getTotalPrice())/transactedPrice);
                    totalPrice = transactedPrice * transactedQuantity;
                    // Label it invalid status temporarily.
                    bestBuy.setStatus(Status.INVALID);
                }

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

            //
            reconcileStockRecords(symbol, bestBuy, bestSell);
        }
        reconcileInvalidTrades(symbol);
    }

    private void reconcileStockRecords(String symbol, Trade bestBuy, Trade bestSell){
        stockRecordService.updateStockRecord(symbol, bestBuy, bestSell);
    }

    /**
     * Register a buy trade on the fund transfer service by deducting the
     * available balance of the appropriate user.
     *
     * The market maker does not need to register trades. Market maker trades
     * are denoted by customerId == 0 and accountId == 0.
     *
     * @param tradeView The trade data that is definable by the trade creator.
     *
     * @return The available balance deducted from the appropriate user.
     * Available balance is determined by the trade's bid price, or the market ask price.
     */
    private Double registerBuyTrade(TradeViewCreatable tradeView) {
        Integer customerId = tradeView.getCustomerId();
        Integer accountId = tradeView.getAccountId();
        if (customerId == 0 && accountId == 0) {
            return 0.0;
        }

        // Get latest stock
        StockRecord latestStock = stockRecordService
                                .getLatestStockRecordBySymbol(tradeView.getSymbol());
        boolean isMarketBuy = tradeView.getBid() == 0;

        Double bid = isMarketBuy
            ? latestStock.getLastAsk() : tradeView.getBid();
        Double availableBalance = bid * tradeView.getQuantity();

        boolean isMarketOpen = dateService.isMarketOpen(tradeView.getSubmittedDate());

        // Update lastBuy on stock records if it is not market buy
        if (bid > latestStock.getLastBid() && !isMarketBuy && isMarketOpen) {
            latestStock.setLastBid(bid);
            stockRecordService.updateStockRecord(tradeView.getSymbol(),
                                                latestStock.getLastBid(),
                                                latestStock.getLastAsk());
        }

        fundTransferService.deductAvailableBalance(
                customerId, accountId,
                availableBalance
        );

        // If bid is higher than
        return availableBalance;
    }

    /**
     * Register a sell trade on the fund transfer service by deducting the
     * appropriate stocks from the user's portfolio.
     *
     * The market maker does not need to register trades.
     *
     * The market maker has customerId = 0 and accountId = 0.
     */
    private void registerSellTrade(TradeViewCreatable tradeView) {
        Integer customerId = tradeView.getCustomerId();
        Integer accountId = tradeView.getAccountId();
        if (customerId == 0 && accountId == 0) {
            return;
        }

        String symbol = tradeView.getSymbol();
        Integer quantity = tradeView.getQuantity();

        // Prevent short selling by checking if the customer has sufficient stock.
        int ownedQuantity = assetService.getAvailableQuantityByPortfolioCustomerIdAndCode(customerId, symbol);
        System.out.println("CUSTOMER " + customerId + " OWNS " + ownedQuantity + " OF " + symbol);
        if (ownedQuantity < quantity) {
            throw new InsufficientStockQuantityException(customerId, symbol);
        }

        // Get latest stock
        StockRecord latestStock = stockRecordService
                                .getLatestStockRecordBySymbol(tradeView.getSymbol());

        boolean isMarketSell = tradeView.getAsk() == 0;

        // If it is a market sell, set to last bid.
        Double ask = isMarketSell
            ? latestStock.getLastBid() : tradeView.getAsk();

        boolean isMarketOpen = dateService.isMarketOpen(tradeView.getSubmittedDate());

        // Update lastAsk on stock records if it is not a market sell
        if ( ask < latestStock.getLastAsk() && !isMarketSell && isMarketOpen){
            latestStock.setLastAsk(ask);
            stockRecordService.updateStockRecord(tradeView.getSymbol(),
                                                latestStock.getLastBid(),
                                                latestStock.getLastAsk());
        }

        // Deducts $0 from the account available balance to check if account belongs to customer.
        fundTransferService.deductAvailableBalance(customerId, accountId, 0.0);
        // TODO: Throw 400? CHECK!!

        // Check if account has enough stocks from portfolio.
        Integer assetAvailableQuantityOwned = portfolioService.getAvailableQuantityOfAsset(customerId, symbol);
        if (quantity > assetAvailableQuantityOwned) {
            throw new InsufficientStockQuantityException(customerId, symbol);
        } else {
            portfolioService.registerSellTrade(customerId, symbol, quantity);
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
            return getEarlierTrade(bestMarket, bestLimit);
        }

        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        System.out.println("limit: "+  bestLimit.getPrice());
        System.out.println("sell: "+ bestSell.getPrice());
        if (bestLimit.getPrice() > bestSell.getPrice()) {
            System.out.println("Best buy : return limit");
            return bestLimit;
        } else if (bestLimit.getPrice().equals(bestSell.getPrice())){
            System.out.println("Best buy : return earlier");
            return getEarlierTrade(bestMarket, bestLimit);
        }
        System.out.println("Best buy : return market");
        return bestMarket;
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
            return getEarlierTrade(bestMarket, bestLimit);
        }
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (bestLimit.getPrice() < bestBuy.getPrice()) {
            System.out.println("Best sell : return limit");
            return bestLimit;
        } else if (bestLimit.getPrice().equals(bestBuy.getPrice())){
            System.out.println("Best sell : return earlier");
            return getEarlierTrade(bestMarket, bestLimit);
        }

        System.out.println("Best sell : return market");
        return bestMarket;
    }

    @Override
    public Trade getBestSellForStockView(String symbol) {
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
            return getEarlierTrade(bestMarket, bestLimit);
        }
        return getEarlierTrade(bestMarket, bestLimit);
    }

    @Override
    public Trade getBestBuyForStockView(String symbol) {
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
            return getEarlierTrade(bestMarket, bestLimit);
        }

        return getEarlierTrade(bestMarket, bestLimit);
    }



    private Trade getBestMarketBuyBySymbol(String symbol) {
        return tradeRepo.findBestMarketBuyBySymbol(symbol).orElse(null);
    }

    private Trade getBestMarketSellBySymbol(String symbol) {
        return tradeRepo.findBestMarketSellBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestLimitBuyBySymbol(String symbol) {
        return tradeRepo.findBestLimitBuyBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestLimitSellBySymbol(String symbol) {
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
            if (trade.getStatus() == Status.INVALID){
                return tradeRepo.save(trade);
            } else if (trade.getFilledQuantity().equals(trade.getQuantity())) {
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
     * Cancel a given trade.
     */
    @Override
    public Trade cancelTrade(Trade trade) {
        if (trade.getStatus() == Status.OPEN || trade.getStatus() == Status.PARTIAL_FILLED) {
            // Restore the available balance of the account.
            fundTransferService.addAvailableBalance(
                    trade.getCustomerId(),
                    trade.getAccountId(),
                    trade.getAvailableBalance());
            trade.setAvailableBalance(0.0);
            trade.setStatus(Status.CANCELLED);
        }
        return tradeRepo.save(trade);
    }

    /**
    *  Cancel trade by ID.
    */
    @Override
    public Trade cancelTrade(Integer tradeId) {
        Trade trade = getTrade(tradeId);
        if (trade == null) {
            throw new TradeNotFoundException(tradeId);
        }
        return cancelTrade(trade);
    }

    /**
    *  Save a market trade
    *
    *  Adds market trade into the market and reconciles the trade
    */
    @Override
    public Trade saveMarketMakerTrade(TradeViewCreatable tradeView) {
        // Save trade.
        Trade trade = tradeView.toTrade();
        tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        //reconcileMarket(tradeView.getSymbol());

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
        stockRecordService.reset();
        fakeScrapingService.loadStockRecords();
        marketMaker.makeNewTrades();
    }


    private Trade getEarlierTrade(Trade trade1, Trade trade2){
        // Return the earlier market buy or limit buy
        return trade1.getSubmittedDate().before(trade2.getSubmittedDate())
        ? trade1 : trade2;
    }

    private Double determineTransactedPriceIfBothMarketOrders(String symbol, Trade bestSell, Trade bestBuy){
        StockRecord latestStock
                    = stockRecordService.getLatestStockRecordBySymbol(symbol);
        Double lastAsk = latestStock.getLastAsk();
        Double lastBid = latestStock.getLastBid();
        if (bestSell.getSubmittedDate().before(bestBuy.getSubmittedDate())){
            return lastAsk;
        } else {
             return lastBid;
        }
    }

    private Double determineTransactedPriceIfBothLimitOrders(Trade bestSell, Trade bestBuy){
        if ( bestBuy.getSubmittedDate().before(bestSell.getSubmittedDate())){
            return bestBuy.getPrice();
        } else {
            return bestSell.getPrice();
        }
    }

    private void reconcileInvalidTrades(String symbol){
        tradeRepo.resetAllInvalidTradesBySymbol(symbol);
        reconcileStockRecords(symbol, getBestBuy(symbol), getBestSell(symbol));
    }

}

