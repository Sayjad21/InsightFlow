package com.insightflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for managing rate limiting to avoid CAPTCHA and LinkedIn blocking.
 * Implements intelligent request spacing and usage tracking.
 */
@Component
public class RateLimitingUtil {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingUtil.class);

    // Rate limiting configuration - increased intervals to avoid CAPTCHA
    private static final long MIN_REQUEST_INTERVAL = 600000; // 10 minutes between requests
    private static final int MAX_REQUESTS_PER_HOUR = 5; // Reduced from 10 to 5
    private static final long HOURLY_RESET_INTERVAL = 3600000; // 1 hour

    // Track usage to avoid rate limiting
    private volatile long lastRequestTime = 0;
    private volatile int requestCount = 0;

    /**
     * Enforces rate limiting before allowing a request to proceed.
     * Blocks the current thread if necessary to maintain proper spacing.
     * 
     * @throws RuntimeException if hourly limit is exceeded or thread is interrupted
     */
    public synchronized void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        
        logger.info("Rate limiting check - Current requests: {}/{}, Last request: {} ms ago",
                requestCount, MAX_REQUESTS_PER_HOUR, currentTime - lastRequestTime);

        // Check if we need to wait due to minimum interval
        if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
            long waitTime = MIN_REQUEST_INTERVAL - (currentTime - lastRequestTime);
            logger.warn("Rate limiting: waiting {} ms ({} minutes) before next request", 
                    waitTime, waitTime / 60000.0);
            
            try {
                Thread.sleep(waitTime);
                currentTime = System.currentTimeMillis(); // Update current time after waiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Request interrupted due to rate limiting", e);
                throw new RuntimeException("Request interrupted due to rate limiting", e);
            }
        }

        // Reset hourly counter if enough time has passed
        if (currentTime - lastRequestTime > HOURLY_RESET_INTERVAL) {
            logger.info("Resetting hourly rate limit counter (was: {})", requestCount);
            requestCount = 0;
        }

        // Check hourly limit
        if (requestCount >= MAX_REQUESTS_PER_HOUR) {
            logger.error("Hourly request limit ({}) exceeded for LinkedIn analysis", MAX_REQUESTS_PER_HOUR);
            throw new RuntimeException("Hourly request limit exceeded. Please wait before making more requests.");
        }

        // Update tracking variables
        lastRequestTime = currentTime;
        requestCount++;
        
        logger.info("Rate limiting passed - Updated count: {}/{}", requestCount, MAX_REQUESTS_PER_HOUR);
    }

    /**
     * Checks if a request would be allowed without actually enforcing the rate limit.
     * Useful for pre-checking before expensive operations.
     * 
     * @return true if request would be allowed, false otherwise
     */
    public synchronized boolean isRequestAllowed() {
        long currentTime = System.currentTimeMillis();

        // Check if we're within the hourly limit (accounting for potential reset)
        if (currentTime - lastRequestTime > HOURLY_RESET_INTERVAL) {
            return true; // Counter would be reset
        }

        // Check if we're under the hourly limit
        if (requestCount >= MAX_REQUESTS_PER_HOUR) {
            return false;
        }

        // Check if enough time has passed since last request
        return currentTime - lastRequestTime >= MIN_REQUEST_INTERVAL;
    }

    /**
     * Gets the time until the next request would be allowed.
     * 
     * @return milliseconds until next request is allowed, 0 if allowed now
     */
    public synchronized long getTimeUntilNextRequest() {
        long currentTime = System.currentTimeMillis();

        // If hourly limit is exceeded and not enough time for reset
        if (requestCount >= MAX_REQUESTS_PER_HOUR && 
            currentTime - lastRequestTime < HOURLY_RESET_INTERVAL) {
            return HOURLY_RESET_INTERVAL - (currentTime - lastRequestTime);
        }

        // Check minimum interval
        if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
            return MIN_REQUEST_INTERVAL - (currentTime - lastRequestTime);
        }

        return 0; // Request allowed now
    }

    /**
     * Gets current rate limiting status information.
     * 
     * @return RateLimitStatus containing current state
     */
    public synchronized RateLimitStatus getStatus() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        boolean wouldReset = timeSinceLastRequest > HOURLY_RESET_INTERVAL;
        
        return new RateLimitStatus(
                wouldReset ? 0 : requestCount,
                MAX_REQUESTS_PER_HOUR,
                timeSinceLastRequest,
                MIN_REQUEST_INTERVAL,
                getTimeUntilNextRequest(),
                isRequestAllowed()
        );
    }

    /**
     * Resets the rate limiting counters. Should be used carefully and only in special circumstances.
     * 
     * @param reason Reason for reset (for logging purposes)
     */
    public synchronized void resetCounters(String reason) {
        logger.warn("Manually resetting rate limiting counters. Reason: {}", reason);
        logger.info("Previous state - Requests: {}/{}, Last request: {} ms ago", 
                requestCount, MAX_REQUESTS_PER_HOUR, System.currentTimeMillis() - lastRequestTime);
        
        requestCount = 0;
        lastRequestTime = 0;
        
        logger.info("Rate limiting counters reset successfully");
    }

    /**
     * Gets the configured minimum interval between requests.
     * 
     * @return minimum interval in milliseconds
     */
    public long getMinRequestInterval() {
        return MIN_REQUEST_INTERVAL;
    }

    /**
     * Gets the configured maximum requests per hour.
     * 
     * @return maximum requests per hour
     */
    public int getMaxRequestsPerHour() {
        return MAX_REQUESTS_PER_HOUR;
    }

    /**
     * Data class containing rate limiting status information.
     */
    public static class RateLimitStatus {
        private final int currentRequests;
        private final int maxRequests;
        private final long timeSinceLastRequest;
        private final long minInterval;
        private final long timeUntilNextRequest;
        private final boolean requestAllowed;

        public RateLimitStatus(int currentRequests, int maxRequests, long timeSinceLastRequest, 
                             long minInterval, long timeUntilNextRequest, boolean requestAllowed) {
            this.currentRequests = currentRequests;
            this.maxRequests = maxRequests;
            this.timeSinceLastRequest = timeSinceLastRequest;
            this.minInterval = minInterval;
            this.timeUntilNextRequest = timeUntilNextRequest;
            this.requestAllowed = requestAllowed;
        }

        public int getCurrentRequests() { return currentRequests; }
        public int getMaxRequests() { return maxRequests; }
        public long getTimeSinceLastRequest() { return timeSinceLastRequest; }
        public long getMinInterval() { return minInterval; }
        public long getTimeUntilNextRequest() { return timeUntilNextRequest; }
        public boolean isRequestAllowed() { return requestAllowed; }

        @Override
        public String toString() {
            return String.format(
                "RateLimitStatus{requests=%d/%d, lastRequest=%dms ago, nextAllowed=%dms, allowed=%s}",
                currentRequests, maxRequests, timeSinceLastRequest, timeUntilNextRequest, requestAllowed
            );
        }
    }
}