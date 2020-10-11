package cs203t10.ryver.market.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.Stock;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockSgxScrapingService implements StockService {

    public StockSgxScrapingService() {
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver");
    }

    @Override
    public List<Stock> getAllStocks() {
        SgxScraper scraper = new SgxScraper();
        return scraper.getAllStocks();
    }

}
