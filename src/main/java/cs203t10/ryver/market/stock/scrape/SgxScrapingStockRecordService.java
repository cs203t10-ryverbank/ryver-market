package cs203t10.ryver.market.stock.scrape;

import java.util.List;

import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.StockRecord;
import cs203t10.ryver.market.stock.StockRecordService;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class SgxScrapingStockRecordService implements StockRecordService {

    public SgxScrapingStockRecordService() {
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver");
    }

    @Override
    public List<StockRecord> getAllStockRecords() {
        SgxScraper scraper = new SgxScraper();
        return scraper.getAllStockRecords();
    }

}
