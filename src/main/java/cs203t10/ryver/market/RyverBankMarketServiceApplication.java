package cs203t10.ryver.market;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cs203t10.ryver.market.trade.TradeService;
import cs203t10.ryver.market.util.DateService;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.ApiOperation;

@SpringBootApplication
@Configuration
@EnableDiscoveryClient
@RestController
@EnableScheduling
public class RyverBankMarketServiceApplication {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private DateService dateService;

    public static void main(String[] args) {
        SpringApplication.run(RyverBankMarketServiceApplication.class, args);
    }

    @GetMapping("/")
    @ApiOperation(value = "Check the service name")
    public String getRoot() {
        return "ryver-market service";
    }

    @PostMapping("/reset")
    @RolesAllowed("MANAGER")
    @ApiOperation(value = "Reset Market Trades")
    public void resetTrades() {
        tradeService.resetTrades();
    }

    @PostMapping("/market/{value}")
    @RolesAllowed("MANAGER")
    @ApiOperation(value = "Artificially open or close the market. Set to either open, close, or default")
    public String setMarketOpen(@PathVariable String value) {
        switch (value) {
            case "open":
                dateService.setOpen(true);
                break;
            case "close":
                dateService.setOpen(false);
                break;
            case "default":
                dateService.setOpen(null);
                break;
            default:
                break;
        }
        return "Market is " + (dateService.isArtificial() ? "artificial" : "default") + ", currently " + dateService.isMarketOpen();
    }

}

