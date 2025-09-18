package com.insightflow.services;

import com.insightflow.utils.ChromeDriverUtil;
import com.insightflow.utils.LinkedInSlugUtil;
import com.insightflow.utils.ContentExtractionUtil;
import com.insightflow.utils.AnalysisOrchestrationUtil;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * Main method to perform LinkedIn analysis for a company.
     * This method demonstrates the clean, modular architecture with focused
     * responsibilities.
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
            // Phase 1: WebDriver setup using dedicated utility
            logger.info("Phase 1: Setting up Chrome WebDriver");
            driver = chromeDriverUtil.createWebDriver();

            // Phase 2: LinkedIn company slug resolution
            logger.info("Phase 2: Resolving LinkedIn company slug for '{}'", companyName);
            String linkedinCompanyId;
            if (linkedinSlug != null && !linkedinSlug.trim().isEmpty()) {
                linkedinCompanyId = linkedinSlug.trim();
                logger.info("✅ Using provided LinkedIn slug: '{}'", linkedinCompanyId);
            } else {
                logger.info("No slug provided, generating one using LinkedInSlugUtil...");
                linkedinCompanyId = linkedInSlugUtil.getLinkedInCompanySlug(companyName);
                logger.info("✅ Generated LinkedIn company slug: '{}'", linkedinCompanyId);
            }

            // Phase 3: Navigate to LinkedIn page
            logger.info("Phase 3: Navigating to LinkedIn page");
            String companyUrl = "https://www.linkedin.com/company/" + linkedinCompanyId + "/";
            driver.get(companyUrl);
            Thread.sleep(3000); // Wait for page load

            // Check for authentication walls or security challenges
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (currentUrl.contains("authwall") || currentUrl.contains("login") || currentUrl.contains("signup")) {
                throw new RuntimeException(
                        "LinkedIn requires authentication - cannot access company page: " + currentUrl);
            }

            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("captcha") || pageSource.contains("security check")) {
                throw new RuntimeException("CAPTCHA or security challenge detected");
            }

            // Phase 4: Content extraction using dedicated utility
            logger.info("Phase 4: Extracting LinkedIn content");
            ContentExtractionUtil.LinkedInContent extractedContent = contentExtractionUtil
                    .extractLinkedInContent(driver, companyName, linkedinCompanyId);

            String companyDescription = extractedContent.description;
            List<String> posts = extractedContent.posts;

            logger.info("✅ Extracted company description: {} chars, posts: {}",
                    companyDescription != null ? companyDescription.length() : 0, posts.size());

            // Phase 5: AI analysis orchestration using dedicated utility
            logger.info("Phase 5: Orchestrating comprehensive AI analysis");
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
            // Phase 6: Cleanup using dedicated utility
            logger.info("Phase 6: Cleaning up resources");
            if (driver != null) {
                driver.quit();
            }
            chromeDriverUtil.cleanupChromeProcesses();
            logger.info("====== CLEANUP COMPLETED ======");
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