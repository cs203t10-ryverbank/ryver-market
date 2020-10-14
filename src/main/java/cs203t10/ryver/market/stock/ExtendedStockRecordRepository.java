package cs203t10.ryver.market.stock;

import java.util.List;
import java.util.Optional;

public interface ExtendedStockRecordRepository {
    List<StockRecord> findAllBySymbol(String symbol);
    List<StockRecord> findAllLatestPerStock();
    Optional<StockRecord> findLatestBySymbol(String symbol);
}

