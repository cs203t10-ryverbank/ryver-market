package cs203t10.ryver.market.stock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import cs203t10.ryver.market.trade.TradeService;

@Profile("test")
@Configuration
public class StockControllerTestConfig {

    @Primary @Bean
    public StockRecordService stockRecordService() {
        return Mockito.mock(StockRecordService.class);
    }

    @Primary @Bean
    public TradeService tradeService() {
        return Mockito.mock(TradeService.class);
    }

}

