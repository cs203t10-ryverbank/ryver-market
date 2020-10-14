package cs203t10.ryver.market.stock.scrape;

import java.util.List;

import cs203t10.ryver.market.stock.StockRecord;

public interface ScrapingService {
    List<StockRecord> loadStockRecords();
}

