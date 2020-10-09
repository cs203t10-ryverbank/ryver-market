package cs203t10.ryver.market.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    @Bean
    public Docket springfoxDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("Ryver Market")
            .select()
            .apis(RequestHandlerSelectors.basePackage("cs203t10.ryver.market"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo())
            .securityContexts(securityContexts())
            .securitySchemes(securitySchemes());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Ryver Market")
            .description("The market service for Ryver Bank API")
            .version("0.0.1")
            .build();
    }

    private List<SecurityContext> securityContexts() {
        return Arrays.asList(
                SecurityContext.builder()
                    .securityReferences(securityReferences())
                    .forPaths(PathSelectors.regex("/.+"))
                    .build());
    }

    private List<SecurityReference> securityReferences() {
        return Arrays.asList(basicAuth(), jwtAuth());
    }

    private SecurityReference basicAuth() {
        return new SecurityReference("basicAuth", new AuthorizationScope[0]);
    }

    private SecurityReference jwtAuth() {
        AuthorizationScope[] authScopes = new AuthorizationScope[1];
        authScopes[0] = new AuthorizationScope("global", "accessEverything");
        return new SecurityReference("JWT", authScopes);
    }

    private List<SecurityScheme> securitySchemes() {
        return Arrays.asList(
            new BasicAuth("basicAuth"),
            new ApiKey("JWT", "Authorization", "header")
        );
    }

}

