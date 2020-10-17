package cs203t10.ryver.market.trade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.fund.FundTransferService;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeView;
import cs203t10.ryver.market.exception.TradeNotFoundException;

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
        // Check if it is market order or limit order.
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

        if (tradeView.getAction() == Action.BUY && tradeView.getBid() != 0.0 && tradeView.getAsk() != 0.0) {
            trade = expiredTrade(trade.getId());
        }

        return getTrade(trade.getId());
    }

    private void reconcileMarket(String symbol){
        Trade bestSell = getBestSell(symbol);
        Trade bestBuy = getBestBuy(symbol);

        // TODO: UPDATE AVG TRADE PRICE
        while (bestSell != null && bestBuy != null) {
            if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0){
            //TODO: GET LAST PRICE IF THERE ARE NO PRICES AVAILABLE
            } else if (bestSell.getPrice() == 0){
                bestSell.setPrice(bestBuy.getPrice());
            } else {
                bestBuy.setPrice(bestSell.getPrice());
            }

            Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
            Integer buyQuantity = bestBuy.getQuantity() - bestBuy.getFilledQuantity();
            if (sellQuantity > buyQuantity){
                bestBuy.setFilledQuantity(bestBuy.getQuantity());
                bestSell.setFilledQuantity(bestSell.getFilledQuantity() + buyQuantity);
            } else{
                bestBuy.setFilledQuantity(bestBuy.getFilledQuantity() + sellQuantity);
                bestSell.setFilledQuantity(bestSell.getFilledQuantity() + sellQuantity);
            }
            updateTrade(bestSell);
            updateTrade(bestBuy);

            // TODO: DEDUCT + ADD ACTUAL BALANCE
            fundTransferService.addBalance(bestSell.getCustomerId(), bestSell.getAccountId(), bestSell.getTotalPrice());
            fundTransferService.deductBalance(bestBuy.getCustomerId(), bestBuy.getAccountId(), bestBuy.getTotalPrice());

            bestSell = getBestSell(symbol);
            bestBuy = getBestBuy(symbol);

            // TODO: MAKE STOCK RECORDS
            stockRecordService.createStockRecord(bestSell.getId());
            stockRecordService.createStockRecord(bestBuy.getId());

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

    // public Double getAvgTradePrice(Trade trade){
    //     Double totalPrice = 0.0;
    //     Integer totalQuantity = 0;
    //     List<Trade> tradeList = tradeRepo.findAll();
    //     for (Trade t : tradeList) {
    //         if (trade.getStock().equals(t.getStock())){
    //             totalPrice += t.getPrice() * t.getQuantity();
    //             totalQuantity += t.getQuantity();
    //         }
    //     }
    //     return (Double) totalPrice/totalQuantity;
    // }

    @Override
    public Trade updateTrade(Trade newTrade) {
        Integer tradeId = newTrade.getId();
        return tradeRepo.findById(tradeId).map(trade -> {
            trade.setPrice(newTrade.getPrice());
            trade.setFilledQuantity(newTrade.getFilledQuantity());            // Set status
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

    public Trade expiredTrade(Integer tradeId) {
        Trade trade = getTrade(tradeId);
        if(trade == null) throw new TradeNotFoundException(tradeId);
        Date currentDate = trade.getSubmittedDate();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,17);
        cal.set(Calendar.MINUTE,0);
        Date fivePM = cal.getTime();
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        String strCurrentTime = formatter.format(currentDate);
        String strFivePM = formatter.format(fivePM);
        String[] partsCurrentTime = strCurrentTime.split(":");
        String[] partsFivePM = strFivePM.split(":");

        if (Integer.parseInt(partsCurrentTime[0]) > Integer.parseInt(partsFivePM[0])) {
            trade.setStatus(Status.EXPIRED);
        } else if (Integer.parseInt(partsCurrentTime[0]) == Integer.parseInt(partsFivePM[0])) {
            if (Integer.parseInt(partsCurrentTime[1]) > Integer.parseInt(partsFivePM[1])) {
                trade.setStatus(Status.EXPIRED);
            }
        } else {
            trade.setStatus(Status.OPEN);
        }
        return tradeRepo.save(trade);
    }

}
