package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.Optional;

public interface ExtendedStockRecordRepository {
    List<StockRecord> findAllByStockSymbol(String symbol);
    List<StockRecord> findAllLatestStockRecords();
    Optional<StockRecord> findLatestStockRecordBySymbol(String symbol);
}

