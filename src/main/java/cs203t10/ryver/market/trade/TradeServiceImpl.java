package cs203t10.ryver.market.trade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;
import cs203t10.ryver.market.exception.TradeInvalidDateException;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private TradeRepository tradeRepo;

    @Override
    public Trade saveTrade(TradeView tradeView) {
        // TODO: Market should open from 9am to 5pm on weekdays only. Check if market is open
        // SHERYLL SOS
        if (openMarket()) {
            // Date tradeDate = tradeView.getSubmittedDate();
            ZoneId defaultZoneId = ZoneId.systemDefault();

            //creating the instance of LocalDate using the day, month, year info
            LocalDateTime localDate = LocalDateTime.now();

            //local date + atStartOfDay() + default time zone + toInstant() = Date
            Date todayDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());

            tradeView.setSubmittedDate(todayDate);

            if (checkInvalidSubmittedDate(tradeView)){

                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                String strCurrentTime = formatter.format(todayDate);
                throw new TradeInvalidDateException(strCurrentTime);
            }
        }

        // If buy trade, deduct available balance.
        if (tradeView.getAction() == Action.BUY) {
            registerBuyTrade(tradeView);
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

    private void reconcileMarket(String symbol){
        Trade bestSell = getBestSell(symbol);
        Trade bestBuy = getBestBuy(symbol);

        while (bestSell != null && bestBuy != null) {
            Double transactedPrice = 0.0;
            if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0){
                // Get last price if there are no prices available.
                StockRecord latestStock = stockRecordService.getLatestStockRecordBySymbol(symbol);
                transactedPrice = latestStock.getPrice();
            } else if (bestSell.getPrice() == 0){
                transactedPrice = bestBuy.getPrice();
            } else {
                transactedPrice = bestSell.getPrice();
            }

            Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
            Integer buyQuantity = bestBuy.getQuantity() - bestBuy.getFilledQuantity();
            Integer transactedQuantity = buyQuantity;
            System.out.println( "sell: " + sellQuantity + " buy: "+ buyQuantity);
            if (sellQuantity < buyQuantity){
                transactedQuantity = sellQuantity;
            }
            Double totalPrice = transactedQuantity * transactedPrice;
            bestBuy.setFilledQuantity(bestBuy.getFilledQuantity() + transactedQuantity);
            bestSell.setFilledQuantity(bestSell.getFilledQuantity() + transactedQuantity);

            // Average price is given by setting total price.
            // Note: avg_price = total price / filled quantity.
            bestBuy.setTotalPrice(totalPrice);
            bestSell.setTotalPrice(totalPrice);

            updateTrade(bestSell);
            updateTrade(bestBuy);

            // Deduct and add actual balance accordingly.
            completeSellTrade(bestSell, totalPrice);
            completeBuyTrade(bestBuy, totalPrice);

            // Make stock records.
            Stock stock = bestBuy.getStock();
            stockRecordService.createStockRecord(stock, transactedPrice, transactedQuantity);

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

    private void completeBuyTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        if (customerId == 0 && accountId == 0) {
            return;
        }
        fundTransferService.deductBalance(customerId, accountId, totalPrice);
        //TODO: reset availableBalance
    }

    private void completeSellTrade(Trade trade, Double totalPrice) {
        Integer customerId = trade.getCustomerId();
        Integer accountId = trade.getAccountId();

        if (customerId == 0 && accountId == 0) {
            return;
        }
        fundTransferService.addBalance( customerId, accountId, totalPrice);
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return tradeRepo.findById(tradeId)
                    .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public Trade getBestBuy(String symbol) {
        Trade bestMarket = getBestMarketBuyBySymbol(symbol);
        Trade bestLimit = getBestLimitBuyBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;
        // The buy with a higher price is better, as it gives the
        // matcher (seller) more per stock traded.
        if (bestLimit.getPrice() > bestMarket.getPrice()) {
            return bestLimit;
        } else if (bestLimit.getPrice() < bestMarket.getPrice()) {
            return bestMarket;
        }
        // If price is the same, then the earlier buy is returned.
        if (bestLimit.getSubmittedDate().before(bestMarket.getSubmittedDate())) {
            return bestLimit;
        }
        return bestMarket;
    }

    @Override
    public Trade getBestSell(String symbol) {
        Trade bestMarket = getBestMarketSellBySymbol(symbol);
        Trade bestLimit = getBestLimitSellBySymbol(symbol);
        if (bestMarket == null && bestLimit == null) return null;
        if (bestLimit == null) return bestMarket;
        if (bestMarket == null) return bestLimit;
        // The sell with a lower price is better, as it lets the
        // matcher (buyer) get more stocks for a lower price.
        if (bestLimit.getPrice() < bestMarket.getPrice()) {
            return bestLimit;
        } else if (bestLimit.getPrice() > bestMarket.getPrice()) {
            return bestMarket;
        }
        // If price is the same, then the earlier sell is returned.
        if (bestLimit.getSubmittedDate().before(bestMarket.getSubmittedDate())) {
            return bestLimit;
        }
        return bestMarket;
    }


    @Override
    public Trade getBestMarketBuyBySymbol(String symbol) {
        return tradeRepo.findBestMarketBuyBySymbol(symbol).orElse(null);
    }

    @Override
    public Trade getBestMarketSellBySymbol(String symbol) {
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
        // TODO: HOW TO GET LOCAL TIMING?
        // SHERYLL SOS
        //default time zone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        //creating the instance of LocalDate using the day, month, year info
        LocalDateTime localDate = LocalDateTime.now();

        //local date + atStartOfDay() + default time zone + toInstant() = Date
        Date todayDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
        // String todayDateStr = new SimpleDateFormat("yyyyMMdd HHmmss").format(todayDate);

        //Displaying LocalDate and Date
        System.out.println("LocalDate is: " + localDate);
        System.out.println("Date is: " + todayDate);
        Date tradeDate = tradeView.getSubmittedDate();

        // Trade is invalid if not made on the current date.
        if (!isSameDay(todayDate,tradeDate)){
            return true;
        }

        // Trade is invalid if it is made before current time.
        if (tradeDate.before(todayDate)){
            return true;
        }

        return false;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }


    @Scheduled(cron = "26 1 * * 1-5 ?")
    public void closeMarket() {
        // Cron expression: close market at 5pm from Monday to Friday.
        List<Trade> tradeList = tradeRepo.findAll();
        for (Trade trade : tradeList) {
            trade.setStatus(Status.EXPIRED);
            tradeRepo.save(trade);
        }
    }

    @Scheduled(cron = "0 9-17 * * 1-5 ?")
    public boolean openMarket() {
        // Cron expression: <minute> <hour> <day-of-month> <month> <day-of-week> <year>
        // Cron expression: open market at 9am - 5pm from Monday to Friday.
        return true;
    }

}
