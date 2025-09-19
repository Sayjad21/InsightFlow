package com.insightflow.services;

import com.insightflow.utils.ChromeDriverUtil;
import com.insightflow.utils.LinkedInSlugUtil;
import com.insightflow.utils.ContentExtractionUtil;
import com.insightflow.utils.AnalysisOrchestrationUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * Refactored and modularized ScrapingService that delegates to focused utility
 * classes.
 * This demonstrates clean code principles with proper separation of concerns.
 * 
 * Original 2051-line monolithic service has been broken down into:
 * - ChromeDriverUtil: WebDriver management and anti-detection
 * - RateLimitingUtil: Request rate control
 * - LinkedInSearchUtil: Company identification and search
 * - ContentExtractionUtil: HTML parsing and content extraction
 * - AnalysisOrchestrationUtil: AI analysis coordination
 */
@Service
public class ModularScrapingService {

    private static final Logger logger = LoggerFactory.getLogger(ModularScrapingService.class);

    @Value("${linkedin.email}")
    private String linkedinEmail;

    @Value("${linkedin.password}")
    private String linkedinPassword;

    @Autowired
    private LinkedInSlugUtil linkedInSlugUtil;

    @Autowired
    private ChromeDriverUtil chromeDriverUtil;

    @Autowired
    private ContentExtractionUtil contentExtractionUtil;

    @Autowired
    private AnalysisOrchestrationUtil analysisOrchestrationUtil;

    @Autowired
    private TavilyFallbackService tavilyFallbackService;

    // Rate limiting to avoid CAPTCHA - same as original ScrapingService
    private final Random random = new Random();
    private long lastRequestTime = 0;
    private int requestCount = 0;
    private static final long MIN_REQUEST_INTERVAL = 300000; // 5 minutes between requests
    private static final int MAX_REQUESTS_PER_HOUR = 5; // Reduced from 10 to 5

    /**
     * Main method to perform LinkedIn analysis for a company.
     * Enhanced with human-like delays, login functionality, and rate limiting.
     * 
     * @param companyName  The name of the company to analyze
     * @param linkedinSlug Optional LinkedIn slug. If null, will be generated
     *                     automatically
     * @return Comprehensive LinkedIn analysis as HTML string
     */
    public String getLinkedInAnalysis(String companyName, String linkedinSlug) {
        logger.info("====== STARTING MODULAR LINKEDIN ANALYSIS FOR: '{}' (slug: '{}') ======", companyName,
                linkedinSlug);
        long analysisStartTime = System.currentTimeMillis();

        WebDriver driver = null;

        try {
            // Rate limiting check to avoid CAPTCHA
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
                long waitTime = MIN_REQUEST_INTERVAL - (currentTime - lastRequestTime);
                logger.info("Rate limiting: Waiting {} ms before next request", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during rate limiting wait", e);
                }
            }
            lastRequestTime = currentTime;
            requestCount++;

            // Phase 1: WebDriver setup using dedicated utility
            logger.info("Phase 1: Setting up Chrome WebDriver");
            driver = chromeDriverUtil.createWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Random delay before starting - increased to be more human-like
            int initialDelay = 5000 + random.nextInt(5000); // 5-10 seconds
            logger.info("Initial human-like delay: {} ms", initialDelay);
            Thread.sleep(initialDelay);

            // Phase 2: Login with human-like behavior
            logger.info("Phase 2: Navigating to LinkedIn login page");
            driver.get("https://www.linkedin.com/login");
            logger.info("Successfully navigated to login page");

            // Random delay to simulate reading
            int readingDelay = 1000 + random.nextInt(2000);
            logger.info("Simulating page reading delay: {} ms", readingDelay);
            Thread.sleep(readingDelay);

            logger.info("Waiting for username field to be present...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            logger.info("✓ Username field located, proceeding with login");

            // Type credentials with human-like delays
            logger.info("Entering login credentials with human-like typing...");
            logger.info("Typing username: {}", linkedinEmail.replaceAll(".(?=.{2})", "*")); // Mask email for security
            typeHumanLike(driver.findElement(By.id("username")), linkedinEmail);

            int betweenFieldsDelay = 500 + random.nextInt(1000);
            logger.info("Delay between username and password fields: {} ms", betweenFieldsDelay);
            Thread.sleep(betweenFieldsDelay);

            logger.info("Typing password: [MASKED]");
            typeHumanLike(driver.findElement(By.id("password")), linkedinPassword);

            int beforeSubmitDelay = 1000 + random.nextInt(1500);
            logger.info("Delay before submit: {} ms", beforeSubmitDelay);
            Thread.sleep(beforeSubmitDelay);

            logger.info("Clicking login submit button...");
            driver.findElement(By.xpath("//button[@type='submit']")).click();

            logger.info("Waiting for login completion (feed or checkpoint)...");
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("linkedin.com/feed"),
                    ExpectedConditions.urlContains("linkedin.com/checkpoint")));

            int postLoginDelay = 3000 + random.nextInt(2000);
            logger.info("Post-login delay: {} ms", postLoginDelay);
            Thread.sleep(postLoginDelay);

            // Check for CAPTCHA or security challenges
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL after login attempt: {}", currentUrl);

            if (currentUrl.contains("checkpoint") || currentUrl.contains("captcha")) {
                logger.error("❌ CAPTCHA or security checkpoint detected at URL: {}", currentUrl);
                throw new RuntimeException("CAPTCHA or security challenge detected after login");
            }

            logger.info("✅ Login successful, no CAPTCHA detected");

            // Phase 3: LinkedIn company slug resolution
            logger.info("Phase 3: Resolving LinkedIn company slug for '{}'", companyName);
            String linkedinCompanyId;
            if (linkedinSlug != null && !linkedinSlug.trim().isEmpty()) {
                linkedinCompanyId = linkedinSlug.trim();
                logger.info("✅ Using provided LinkedIn slug: '{}'", linkedinCompanyId);
            } else {
                logger.info("No slug provided, generating one using LinkedInSlugUtil...");
                linkedinCompanyId = linkedInSlugUtil.getLinkedInCompanySlug(companyName);
                logger.info("✅ Generated LinkedIn company slug: '{}'", linkedinCompanyId);
            }

            // Phase 4: Navigate to LinkedIn company page
            logger.info("Phase 4: Navigating to LinkedIn company page");
            String companyUrl = "https://www.linkedin.com/company/" + linkedinCompanyId + "/";
            logger.info("Company URL: {}", companyUrl);

            long pageLoadStartTime = System.currentTimeMillis();
            driver.get(companyUrl);
            long pageLoadEndTime = System.currentTimeMillis();

            logger.info("✅ Company page loaded in {} ms", pageLoadEndTime - pageLoadStartTime);
            Thread.sleep(5000); // Wait for full page load

            // Phase 5: Content extraction using dedicated utility
            logger.info("Phase 5: Extracting LinkedIn content");
            ContentExtractionUtil.LinkedInContent extractedContent = contentExtractionUtil
                    .extractLinkedInContent(driver, companyName, linkedinCompanyId);

            String companyDescription = extractedContent.description;
            List<String> posts = extractedContent.posts;

            logger.info("✅ Extracted company description: {} chars, posts: {}",
                    companyDescription != null ? companyDescription.length() : 0, posts.size());

            // Phase 6: AI analysis orchestration using dedicated utility
            logger.info("Phase 6: Orchestrating comprehensive AI analysis");
            String analysis = analysisOrchestrationUtil.orchestrateLinkedInAnalysis(companyName,
                    extractedContent.companyTitle, companyDescription, posts);

            long totalDuration = System.currentTimeMillis() - analysisStartTime;
            logger.info("====== MODULAR LINKEDIN ANALYSIS COMPLETED FOR '{}' in {}ms ======", companyName,
                    totalDuration);

            return analysis;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - analysisStartTime;
            logger.error("❌ Modular LinkedIn analysis failed for '{}' after {}ms", companyName, totalDuration);
            logger.error("Error: {}", e.getMessage(), e);

            // Fallback using TavilyFallbackService with slug support
            try {
                logger.warn("🔄 Attempting Tavily fallback for LinkedIn analysis...");
                String fallbackResult = tavilyFallbackService.getLinkedInAnalysisFallback(companyName, linkedinSlug);
                logger.info("✅ Tavily fallback successful for company: {}", companyName);
                return fallbackResult;
            } catch (Exception fallbackException) {
                logger.error("❌ Tavily fallback also failed: {}", fallbackException.getMessage());
                throw new RuntimeException("Failed to perform LinkedIn analysis for " + companyName +
                        " after " + totalDuration + "ms. Primary error: " + e.getMessage() +
                        ". Fallback error: " + fallbackException.getMessage(), e);
            }
        } finally {
            // Phase 7: Cleanup using dedicated utility
            logger.info("Phase 7: Cleaning up resources");
            if (driver != null) {
                driver.quit();
            }
            chromeDriverUtil.cleanupChromeProcesses();
            logger.info("====== CLEANUP COMPLETED ======");
        }
    }

    /**
     * Types text with human-like delays to avoid detection (from original
     * ScrapingService)
     */
    private void typeHumanLike(WebElement element, String text) {
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(50 + random.nextInt(100)); // Random delay between keystrokes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Legacy method for backward compatibility - automatically generates slug
     * 
     * @param companyName The name of the company to analyze
     * @return Comprehensive LinkedIn analysis as HTML string
     */
    public String getLinkedInAnalysis(String companyName) {
        return getLinkedInAnalysis(companyName, null);
    }
}