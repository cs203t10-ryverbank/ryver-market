package cs203t10.ryver.market.stock;

import java.util.List;

public interface ExtendedStockRecordRepository {
    List<StockRecord> findAllByStockSymbol(String symbol);
}

