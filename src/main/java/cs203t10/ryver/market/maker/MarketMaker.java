package cs203t10.ryver.market.maker;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;
import cs203t10.ryver.market.trade.TradeRepository;
import cs203t10.ryver.market.trade.TradeService;
import cs203t10.ryver.market.trade.Trade.Action;
import cs203t10.ryver.market.trade.Trade.Status;
import cs203t10.ryver.market.trade.view.TradeViewViewable;

// TODO: At 9am, inject liquidity - can use cron Expression

@Service
public class MarketMaker {

    public static final int MIN_QUANTITY = 20_000;
    public static final double NEW_BID_RATIO = 0.9;
    public static final double NEW_ASK_RATIO = 1.1;

    @Autowired
    private TradeRepository tradeRepo;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private StockRecordService stockRecordService;

    public void makeNewTrades() {
        List<StockRecord> latestRecords = stockRecordService.getAllLatestStockRecords();
        for (StockRecord record : latestRecords) {
            String symbol = record.getStock().getSymbol();
            Double bid = record.getPrice() * NEW_BID_RATIO;
            Double ask = record.getPrice() * NEW_ASK_RATIO;
            makeNewBuyTradesAtPrice(symbol, bid);
            makeNewSellTradesAtPrice(symbol, ask);
            stockRecordService.updateStockRecord(symbol, bid, ask);
        }
    }

    public void makeNewBuyTradesAtPrice(String symbol, Double price) {
        // The liquid quantity = Total Quantity - Filled Quantity
        Long totalQuantity = tradeRepo.getBuyQuantityBySymbol(symbol) - tradeRepo.getBuyFilledQuantityBySymbol(symbol);
        // If liquidity is low, then make new trades
        if (totalQuantity < MIN_QUANTITY) {
            tradeService.saveMarketMakerTrade(TradeViewViewable.builder()
                    .action(Action.BUY)
                    .symbol(symbol)
                    .quantity((int) (MIN_QUANTITY - totalQuantity))
                    .customerId(0)
                    .accountId(0)
                    .submittedDate(new Date())
                    .status(Status.OPEN)
                    .bid(price)
                    .build());
        }
    }

    public void makeNewSellTradesAtPrice(String symbol, Double price) {
        // The liquid quantity = Total Quantity - Filled Quantity
        Long totalQuantity = tradeRepo.getSellQuantityBySymbol(symbol)
                - tradeRepo.getSellFilledQuantityBySymbol(symbol);
        // If liquidity is low, then make new trades
        if (totalQuantity < MIN_QUANTITY) {
            tradeService.saveMarketMakerTrade(TradeViewViewable.builder()
                    .action(Action.SELL)
                    .symbol(symbol)
                    .quantity((int) (MIN_QUANTITY - totalQuantity))
                    .customerId(0)
                    .accountId(0)
                    .submittedDate(new Date())
                    .status(Status.OPEN)
                    .ask(price)
                    .build());
        }
    }

}

