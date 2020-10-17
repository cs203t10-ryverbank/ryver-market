package cs203t10.ryver.market.stock;

import java.util.List;

public interface StockRecordService {
    List<StockRecord> getAllLatestStockRecords();
    StockRecord getLatestStockRecordBySymbol(String symbol);
    StockRecord createStockRecord(Integer tradeId);
}
