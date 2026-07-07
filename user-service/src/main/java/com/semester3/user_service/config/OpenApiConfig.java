package com.semester3.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("API documentation for the User Service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gbenga Famodun")
                                .email("gbengeneh55@gmail.com")
                                .url("https://github.com/gbengeneh"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
