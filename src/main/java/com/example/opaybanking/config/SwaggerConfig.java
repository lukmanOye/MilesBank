package com.example.opaybanking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI milesBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Miles Bank API")
                        .description("Nigeria's Fastest Digital Banking Backend — Built by Oyedokun Lukman")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Oyedokun Lukman")
                                .email("oyedokun.lukmanoye@gmail.com")
                                .url("https://github.com/lukmanOye")));
    }
}