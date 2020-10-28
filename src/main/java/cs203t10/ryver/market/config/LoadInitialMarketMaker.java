package cs203t10.ryver.market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cs203t10.ryver.market.maker.MarketMaker;

@Component
@Order(2)
public class LoadInitialMarketMaker implements CommandLineRunner {

    @Autowired
    private MarketMaker marketMaker;

    @Override
    public void run(String... args) throws Exception {
        marketMaker.makeNewTrades();
    }

}

