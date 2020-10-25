package cs203t10.ryver.market.trade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.portfolio.Portfolio;
import cs203t10.ryver.market.portfolio.PortfolioService;
import cs203t10.ryver.market.portfolio.asset.Asset;
import cs203t10.ryver.market.portfolio.asset.AssetService;
import cs203t10.ryver.market.portfolio.asset.InsufficientStockQuantityException;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.exception.*;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;
import cs203t10.ryver.market.maker.MarketMaker;

@Component
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TradeRepository tradeRepo;

    @Autowired
    private MarketMaker marketMaker;

    @Override
    public Trade saveTrade(TradeView tradeView) {
        Date todayDate = getCurrentDate();
        tradeView.setSubmittedDate(todayDate);

        // Checks for valid date.
        if (checkInvalidSubmittedDate(tradeView)){
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String strCurrentTime = formatter.format(todayDate);
            //throw new TradeInvalidDateException(strCurrentTime);
        }

        // If buy trade, deduct available balance.
        if (tradeView.getAction() == Action.BUY) {
            registerBuyTrade(tradeView);
        }

        // If sell trade, add to stock records and check if stocks available.
        if (tradeView.getAction() == Action.SELL) {
            registerSellTrade(tradeView);
        }

        // By default, trade will be set to OPEN status.
        tradeView.setStatus(Status.OPEN);

        // Save trade.
        Trade trade = tradeView.toTrade();
        tradeRepo.saveWithSymbol(trade, tradeView.getSymbol());

        // Reconcile market status after adding trade.
        reconcileMarket(tradeView.getSymbol());

        // EDIT: See closeMarket() method. Uses @Scheduled to close market every time it is 5pm.
        // If limit order, expire the trade by 5PM.

        return getTrade(trade.getId());
    }

    /**
     * Reconcile market
     * Register a buy trade on the fund transfer service by deducting the
     * available balance of the appropriate user.
     *
     * The market maker does not need to register trades.
     *
     * The market maker has customerId = 0 and accountId = 0.
     */
    private void reconcileMarket(String symbol){
        Trade bestSell = getBestSell(symbol);
        Trade bestBuy = getBestBuy(symbol);
        System.out.println("Sell: " + (bestSell == null));
        System.out.println("Buy: " + (bestBuy == null));

        // The market undergoes reconciliation as long as there is a bestSell and bestBuy.
        while (bestSell != null && bestBuy != null) {
            // Determine transactedPrice.
            Double transactedPrice = 0.0;
            if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0){
                // Get last price if there are no prices available.
                StockRecord latestStock = stockRecordService.getLatestStockRecordBySymbol(symbol);
                transactedPrice = latestStock.getPrice();
            } else if (bestSell.getPrice() == 0){
                transactedPrice = bestBuy.getPrice();
            } else if (bestBuy.getPrice() == 0 || bestBuy.getPrice() > bestSell.getPrice()){
                transactedPrice = bestSell.getPrice();
            } else if (bestBuy.getPrice() < bestSell.getPrice()){
                return;
            }

            // Determine transactedQuantity.
            Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
            Integer buyQuantity = bestBuy.getQuantity() - bestBuy.getFilledQuantity();
            Integer transactedQuantity = buyQuantity;
            if (sellQuantity < buyQuantity){
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

        // TODO: HOTFIX write proper method to check if account belongs customerId?
        // Deducts $0 from the account available balance to check if account belongs to customer.
        fundTransferService.deductAvailableBalance(customerId, accountId, 0.0);

        // Check if account has enough stocks from portfolio.
        Asset asset = assetService.findByPortfolioCustomerIdAndCode(customerId, symbol);
        Integer quantityOwned = assetService.getQuantityOfAsset(asset);
        if (quantityOwned < quantity) {
            throw new InsufficientStockQuantityException(customerId, symbol);
        }

        // Add to stock records
        stockRecordService.updateStockRecordAddToMarket(symbol, quantity);
    }

    private void completeBuyTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        // Ignore trades made by market maker.
        if (customerId == 0 && accountId == 0) {
            return;
        }

        // Add stocks to buyer portfolio
        Portfolio portfolio = portfolioService.processBuyTrade(trade);

        // Deduct balance from buyer's account.
        fundTransferService.deductBalance(customerId, accountId, totalPrice);
    }

    private void completeSellTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        if (customerId == 0 && accountId == 0) {
            return;
        }
        // Deduct stocks from seller portfolio.
        Portfolio portfolio = portfolioService.processSellTrade(trade);

        // Add balance to seller's account.
        fundTransferService.addBalance(customerId, accountId, totalPrice);
        fundTransferService.addAvailableBalance(customerId, accountId, totalPrice);
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId)
                    .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public Trade getBestBuy(String symbol) {
        Trade bestSell =getBestLimitSellBySymbol(symbol);
        Trade bestMarket = getBestMarketBuyBySymbol(symbol);
        Trade bestLimit = getBestLimitBuyBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;

        if (bestSell == null) {
            return bestMarket.getSubmittedDate().before(bestLimit.getSubmittedDate())
                ? bestMarket : bestLimit;
        }

        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        if (bestLimit.getPrice() > bestSell.getPrice()) {
            return bestLimit;
        } else {
            return bestMarket;
        }
    }

    @Override
    public Trade getBestSell(String symbol) {
        Trade bestBuy =getBestLimitBuyBySymbol(symbol);
        Trade bestMarket = getBestMarketSellBySymbol(symbol);
        Trade bestLimit = getBestLimitSellBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;

        if (bestBuy == null) {
            return bestMarket.getSubmittedDate().before(bestLimit.getSubmittedDate())
                ? bestMarket : bestLimit;
        }
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (bestLimit.getPrice() < bestBuy.getPrice()) {
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

    @Override
    public Trade updateTrade(Trade newTrade) {
        Integer tradeId = newTrade.getId();
        return tradeRepo.findById(tradeId).map(trade -> {
            trade.setPrice(newTrade.getPrice());
            trade.setFilledQuantity(newTrade.getFilledQuantity());

            // Set status
            if (trade.getFilledQuantity().equals(trade.getQuantity())){
                trade.setStatus(Status.FILLED);
            } else if (trade.getFilledQuantity() > 0){
                trade.setStatus(Status.PARTIAL_FILLED);
            } else {
                trade.setStatus(Status.OPEN);
            }
            return tradeRepo.save(trade);
        }).orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public Trade cancelTrade(Integer tradeId) {
        Trade trade = getTrade(tradeId);
        if(trade == null) throw new TradeNotFoundException(tradeId);
        trade.setStatus(Status.CANCELLED);
        return tradeRepo.save(trade);
    }

    public boolean checkInvalidSubmittedDate(TradeView tradeView) {
        // Returns true if date is invalid
        // Check if the post is made on a weekday, between 9am and 5pm.
        Date todayDate = getCurrentDate();
        Date tradeDate = tradeView.getSubmittedDate();

        // Trade is made between 9am and 5pm.
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String strCurrentTime = formatter.format(tradeDate);
        LocalTime target = LocalTime.parse(strCurrentTime);

        // NOTE: Commented segment off to test. Please uncomment before deploying.
        // if (target.isBefore(LocalTime.parse("09:00:00")) || target.isAfter(LocalTime.parse("17:00:00")) ){
        //     return true;
        // }

        // Trade is invalid if not made on the current date.
        if (!isSameDay(todayDate,tradeDate)){
            return true;
        }

        // Trade is invalid if it is made before current time.
        if (tradeDate.before(todayDate)){
            return true;
        }

        // Checks if it is a weekday
        Calendar cal = Calendar.getInstance();
        cal.setTime(tradeDate);
        if ((cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) || cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            return true;
        }
        return false;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    private Date getCurrentDate() {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDateTime localDate = LocalDateTime.now();
        Date todayDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
        return todayDate;
    }

    // TODO: Verify this works on AWS
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Singapore")
    public void closeMarket() {
        // Cron expression: close market at 5pm from Monday to Friday.
        List<Trade> tradeList = tradeRepo.findAll();
        Set<String> customerAccountSet = new HashSet<>();
        List<Integer[]> customerAccountList = new ArrayList<>();

        // Close trades for all trades in the tradeList
        for (Trade trade : tradeList) {
            Integer customerId = trade.getCustomerId();
            Integer accountId = trade.getAccountId();
            String uniqueCustomerAccount = Integer.toString(customerId) + Integer.toString(accountId);
            if (trade.getStatus().equals(Status.EXPIRED)){
                continue;
            }
            if (customerId == 0 && accountId == 0) {
                continue;
            }
            trade.setStatus(Status.EXPIRED);

            // Checks if customer-account pair has already been added to the list.
            if (!customerAccountSet.contains(uniqueCustomerAccount)){
                customerAccountSet.add(uniqueCustomerAccount);
                Integer[] customerAccountPair = {customerId, accountId};
                customerAccountList.add(customerAccountPair);
            }
            tradeRepo.save(trade);
        }

        // For each account-customer pair, reset available balance.
        for (Integer[] pair : customerAccountList){
            Integer customerId = pair[0];
            Integer accountId = pair[1];
            // reset balance using FTS
            fundTransferService.resetAvailableBalance(customerId, accountId);
        }
    }

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
    public List <Trade> getAllSellTradesBySymbol(String symbol){
        return tradeRepo.findAllSellTradesBySymbol(symbol);
    };

    @Override
    public List <Trade> getAllBuyTradesBySymbol(String symbol){
        return tradeRepo.findAllBuyTradesBySymbol(symbol);
    };

    @Override
    public Integer getTotalBidVolume(String symbol){
        List<Trade> buyTrades = getAllBuyTradesBySymbol(symbol);
        Integer totalBuyFilledQuantity = 0;
        Integer totalBuyQuantity = 0;
        for (Trade trade : buyTrades){
            totalBuyQuantity += trade.getQuantity();
            totalBuyFilledQuantity += trade.getFilledQuantity();

            System.out.println("Buy Qty: "+ trade.getQuantity());
            System.out.println("Buy Filled Qty: "+ trade.getFilledQuantity());
        }
        return totalBuyQuantity - totalBuyFilledQuantity;
    }

    @Override
    public Integer getTotalAskVolume(String symbol){
        List<Trade> sellTrades = getAllSellTradesBySymbol(symbol);
        Integer totalSellFilledQuantity = 0;
        Integer totalSellQuantity = 0;
        for (Trade trade : sellTrades){
            System.out.println("Ask Qty: "+ trade.getQuantity());
            System.out.println("Ask Filled Qty: "+ trade.getFilledQuantity());
            totalSellQuantity += trade.getQuantity();
            totalSellFilledQuantity += trade.getFilledQuantity();
        }
        return totalSellQuantity - totalSellFilledQuantity;
    }

}
