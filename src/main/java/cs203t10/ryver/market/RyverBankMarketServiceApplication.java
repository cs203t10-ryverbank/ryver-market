package cs203t10.ryver.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "Check the service name")
    public String getRoot() {
        return "ryver-market service";
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}

