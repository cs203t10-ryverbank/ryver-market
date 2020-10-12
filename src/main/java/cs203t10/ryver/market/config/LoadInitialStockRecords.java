package cs203t10.ryver.market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cs203t10.ryver.market.stock.scrape.ScrapingService;

@Component
@Order(1)
public class LoadInitialStockRecords implements CommandLineRunner {

    @Autowired
    ScrapingService scrapingService;

    @Override
    public void run(String... args) throws Exception {
        scrapingService.loadStockRecords();
        System.out.println("STI records loaded from SGX");
    }
}
