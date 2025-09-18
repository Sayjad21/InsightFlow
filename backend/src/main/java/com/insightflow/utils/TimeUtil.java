package com.insightflow.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeUtil {

    @Value("${app.timezone:Asia/Dhaka}")
    private String applicationTimezone;

    /**
     * Get current time in the configured application timezone
     */
    public LocalDateTime now() {
        return ZonedDateTime.now(ZoneId.of(applicationTimezone)).toLocalDateTime();
    }

    /**
     * Get current time as formatted string in application timezone
     */
    public String nowAsString() {
        return now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Get current time as ISO string in application timezone
     */
    public String nowAsIsoString() {
        return now().toString();
    }

    /**
     * Convert UTC time to application timezone
     */
    public LocalDateTime utcToLocal(LocalDateTime utcTime) {
        return utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of(applicationTimezone))
                .toLocalDateTime();
    }

    /**
     * Convert local time to UTC
     */
    public LocalDateTime localToUtc(LocalDateTime localTime) {
        return localTime.atZone(ZoneId.of(applicationTimezone))
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Get current timestamp for filename generation
     */
    public String getTimestampForFilename() {
        return now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * Get application timezone
     */
    public String getApplicationTimezone() {
        return applicationTimezone;
    }
}