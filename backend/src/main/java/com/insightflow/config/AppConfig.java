package com.insightflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean("appRestTemplate") // Renamed bean to avoid conflict
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}