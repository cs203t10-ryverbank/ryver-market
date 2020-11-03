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
import cs203t10.ryver.market.util.DateUtils;

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

    @PostMapping("/market/flip-open/{value}")
    @RolesAllowed("MANAGER")
    @ApiOperation(value = "Flip or unflip market open or close")
    public String toggleDateUtil(@PathVariable boolean value) {
        DateUtils.flipped = value;
        return "Market is " + (DateUtils.flipped ? "flipped" : "regular");
    }

}

