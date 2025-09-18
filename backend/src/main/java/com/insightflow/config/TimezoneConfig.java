package com.insightflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    @Value("${app.timezone:Asia/Dhaka}")
    private String applicationTimezone;

    @PostConstruct
    public void configureTimezone() {
        // Set the default JVM timezone
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
        System.out.println("Application timezone set to: " + applicationTimezone);
    }
}