package cs203t10.ryver.market.stock.scrape;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordRepository;
import cs203t10.ryver.market.stock.StockRepository;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
public final class SgxScrapingService implements ScrapingService {

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private StockRecordRepository stockRecordRepo;

    public SgxScrapingService() {
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver");
    }

    public List<StockRecord> loadStockRecords() {
        SgxScraper scraper = new SgxScraper();
        List<StockRecord> newRecords = scraper.getAllStockRecords();

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

