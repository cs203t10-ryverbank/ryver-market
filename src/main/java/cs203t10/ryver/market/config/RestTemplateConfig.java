package cs203t10.ryver.market.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public final class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder.build();
    }

}

