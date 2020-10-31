package cs203t10.ryver.market.stock;

import java.util.List;

public interface StockRecordService {
    List<StockRecord> getAllLatestStockRecords();
    StockRecord getLatestStockRecordBySymbol(String symbol);
    StockRecord updateStockRecordRemoveFromMarket(String symbol, Double price, Integer quantity);
    StockRecord updateStockRecordAddToMarket(String symbol, Integer quantity);
    public StockRecord updateStockRecord(String symbol, Double lastBid, Double lastAsk);
}
