package com.example.opaybanking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI milesBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Miles Bank API")
                        .description("Nigeria's Fastest Digital Bank Backend - Built by Oyedokun Lukman")
                        .version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("milesbank")
                .pathsToMatch("/api/**")
                .packagesToScan("com.example.opaybanking.controller")
                .build();
    }
}