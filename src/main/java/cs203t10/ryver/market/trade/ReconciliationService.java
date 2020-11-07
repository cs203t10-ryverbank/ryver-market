package cs203t10.ryver.market.trade;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.util.DoubleUtils;

@Service
public class ReconciliationService {

    @Autowired
    private StockRecordService stockRecordService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TradeRepository tradeRepo;

    /**
     * Reconcile the market by searching for matching buy and sell trades.
     *
     * If market trades are matched, then they will be completed on the fund
     * transfer service, then remove the trade quantity from the stock records.
     */
    @Transactional
    public void reconcileMarket(String symbol) {
        Trade bestSell = tradeService.getBestSell(symbol);
        Trade bestBuy = tradeService.getBestBuy(symbol);
        // The market undergoes reconciliation as long as there is a bestSell
        // and bestBuy.
        while (bestSell != null && bestBuy != null) {
            // Determine transactedPrice.
            Double transactedPrice = determineTransactedPrice(bestBuy, bestSell);
            if (transactedPrice < 0.0){
                break;
            }
            Boolean isMarketBuy = bestBuy.getPrice() == 0;

            // Determine transactedQuantity.
            Integer transactedQuantity = determineTransactedQuantity(bestBuy, bestSell);


            // Update filledQuantity and totalPrice for trades.
            Double totalPrice = transactedQuantity * transactedPrice;
            // If market buy, check if available balance is sufficient
            if (isMarketBuy && totalPrice + bestBuy.getTotalPrice() > bestBuy.getAvailableBalance()) {
                transactedQuantity = DoubleUtils
                    .getRoundedToNearestHundred(
                            (bestBuy.getAvailableBalance() - bestBuy.getTotalPrice()) / transactedPrice);
                totalPrice = transactedPrice * transactedQuantity;
                // Label it invalid status temporarily.
                bestBuy.setStatus(Status.INVALID);
            }

            bestBuy.setFilledQuantity(bestBuy.getFilledQuantity() + transactedQuantity);
            bestSell.setFilledQuantity(bestSell.getFilledQuantity() + transactedQuantity);
            bestBuy.setTotalPrice(bestBuy.getTotalPrice() + totalPrice);
            bestSell.setTotalPrice(bestSell.getTotalPrice() + totalPrice);

            // Update trade.
            tradeService.updateTrade(bestSell);
            tradeService.updateTrade(bestBuy);

            // Deduct and add actual balance accordingly.
            tradeService.completeSellTrade(bestSell, totalPrice);
            tradeService.completeBuyTrade(bestBuy, totalPrice);

            // Make stock records.
            // Transacted quantity is recorded as negative, as these stocks are leaving the market.
            // Total volume of stocks in stock records will decrease.
            stockRecordService.updateStockRecordRemoveFromMarket(symbol, transactedPrice, transactedQuantity);

            // Get new bestSell and bestBuy.
            bestSell = tradeService.getBestSell(symbol);
            bestBuy = tradeService.getBestBuy(symbol);

            reconcileStockRecords(symbol, bestBuy, bestSell);
        }
        reconcileInvalidTrades(symbol);
    }

    public void reconcileStockRecords(String symbol, Trade bestBuy, Trade bestSell){
        stockRecordService.updateStockRecord(symbol, bestBuy, bestSell);
    }

    public void reconcileInvalidTrades(String symbol){
        tradeRepo.resetAllInvalidTradesBySymbol(symbol);
        reconcileStockRecords(
                symbol,
                tradeService.getBestBuy(symbol),
                tradeService.getBestSell(symbol));
    }

    private double determineTransactedPrice(Trade bestBuy, Trade bestSell){
        if (bestSell.getPrice() == 0 && bestBuy.getPrice() == 0) {
            return tradeService.determineTransactedPriceIfBothMarketOrders(bestSell, bestBuy);
        } else if (bestSell.getPrice() == 0) {
            return bestBuy.getPrice();
        } else if (bestBuy.getPrice() == 0) {
            return bestSell.getPrice();
        } else if (bestBuy.getPrice() >= bestSell.getPrice()){
            return tradeService.determineTransactedPriceIfBothLimitOrders(bestSell, bestBuy);
        } else if (bestBuy.getPrice() < bestSell.getPrice()) {
            return -1.0; // No suitable trade is found
        }
        return -1.0;
    }

    private Integer determineTransactedQuantity(Trade bestBuy, Trade bestSell){
        // Determine transactedQuantity.
        Integer sellQuantity = bestSell.getQuantity() - bestSell.getFilledQuantity();
        Integer buyQuantity = bestBuy.getQuantity() - bestBuy.getFilledQuantity();
        Integer transactedQuantity = buyQuantity;
        if (sellQuantity < buyQuantity) {
            transactedQuantity = sellQuantity;
        }
        return transactedQuantity;
    }


}

