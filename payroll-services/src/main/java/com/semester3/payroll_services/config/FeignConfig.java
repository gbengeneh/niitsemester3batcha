package com.semester3.payroll_services.config;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign client configuration — applies to all @FeignClient interfaces
 * (currently just EmployeeClient).
 */
@Configuration
public class FeignConfig {

    @Bean
    public Request.Options options() {
        // connectTimeout, readTimeout
        return new Request.Options(
            5, TimeUnit.SECONDS,
            10, TimeUnit.SECONDS,
            true
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new EmployeeClientErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
