package cs203t10.ryver.market.stock.scrape;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordRepository;
import cs203t10.ryver.market.stock.StockRepository;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class FakeScrapingService implements ScrapingService {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    StockRecordRepository stockRecordRepo;

    public List<StockRecord> loadStockRecords() {
        FakeScraper scraper = new FakeScraper();
        List<StockRecord> newRecords = scraper.buildFakeRecords();

        // Store all newly discovered stocks.
        newRecords.stream()
            .map(StockRecord::getStock)
            .map(stockRepo::save)
            .forEach(System.out::println);
        // Only add records for stocks without any records.
        newRecords.stream()
            // Find stocks with no records.
            .filter(record -> {
                String symbol = record.getStock().getSymbol();
                return stockRecordRepo.findLatestBySymbol(symbol).isEmpty();
            })
            .map(stockRecordRepo::save)
            .forEach(System.out::println);
        return newRecords;
    }

}

