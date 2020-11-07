package cs203t10.ryver.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.Hidden;

@SpringBootApplication
@Configuration
@EnableDiscoveryClient
@RestController
@EnableScheduling
public class RyverBankMarketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RyverBankMarketServiceApplication.class, args);
    }

    @GetMapping("/")
    @Hidden
    public String getRoot() {
        return "ryver-market service";
    }
}

