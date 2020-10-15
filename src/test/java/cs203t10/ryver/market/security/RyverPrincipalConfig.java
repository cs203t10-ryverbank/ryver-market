package cs203t10.ryver.market.security;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class RyverPrincipalConfig {

    @Primary @Bean
    public PrincipalService principalService() {
        return Mockito.mock(PrincipalService.class);
    }

}

