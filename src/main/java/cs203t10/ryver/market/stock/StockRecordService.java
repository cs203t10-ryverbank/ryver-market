package cs203t10.ryver.market.stock;

import java.util.List;

import cs203t10.ryver.market.trade.Trade;
import cs203t10.ryver.market.trade.view.TradeViewCreatable;

public interface StockRecordService {
    List<StockRecord> getAllLatestStockRecords();
    StockRecord getLatestStockRecordBySymbol(String symbol);
    StockRecord updateStockRecordRemoveFromMarket(String symbol, Double price, Integer quantity);
    StockRecord updateStockRecordAddToMarket(String symbol, Integer quantity);
    StockRecord updateStockRecord(String symbol, Double lastBid, Double lastAsk);
    StockRecord updateStockRecord(String symbol, Trade bestBuy, Trade bestSell);
    void updateLastBidOnStockRecord(TradeViewCreatable tradeView);
    void updateLastAskOnStockRecord(TradeViewCreatable tradeView);
    void reset();
}
