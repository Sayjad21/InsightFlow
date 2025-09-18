package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import com.insightflow.utils.TavilyUtil;
import com.insightflow.utils.ChromeDriverUtil;
import com.insightflow.utils.LinkedInSearchUtil;
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
 * Refactored and modularized ScrapingService that delegates to focused utility classes.
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
    private AiUtil aiUtil;

    @Autowired
    private TavilyUtil tavilyUtil;

    @Autowired
    private ChromeDriverUtil chromeDriverUtil;

    @Autowired
    private LinkedInSearchUtil linkedInSearchUtil;

    @Autowired
    private ContentExtractionUtil contentExtractionUtil;

    @Autowired
    private AnalysisOrchestrationUtil analysisOrchestrationUtil;

    @Autowired
    private TavilyFallbackService tavilyFallbackService;

    /**
     * Main method to perform LinkedIn analysis for a company.
     * This method demonstrates the clean, modular architecture with focused responsibilities.
     * 
     * @param companyName The name of the company to analyze
     * @return Comprehensive LinkedIn analysis as HTML string
     */
    public String getLinkedInAnalysis(String companyName) {
        logger.info("====== STARTING MODULAR LINKEDIN ANALYSIS FOR: '{}' ======", companyName);
        long analysisStartTime = System.currentTimeMillis();

        WebDriver driver = null;

        try {
            // Phase 2: WebDriver setup using dedicated utility (now becomes Phase 1)
            logger.info("Phase 1: Setting up Chrome WebDriver");
            driver = chromeDriverUtil.createWebDriver();

            // Phase 3: LinkedIn company identification (now becomes Phase 2)
            logger.info("Phase 2: Getting LinkedIn company ID for '{}'", companyName);
            String linkedinCompanyId = linkedInSearchUtil.getLinkedInCompanyId(companyName);
            logger.info("✅ Found LinkedIn company ID: '{}'", linkedinCompanyId);

            // Phase 4: Navigate to LinkedIn page (now becomes Phase 3)
            logger.info("Phase 3: Navigating to LinkedIn page");
            String companyUrl = "https://www.linkedin.com/company/" + linkedinCompanyId + "/";
            driver.get(companyUrl);
            Thread.sleep(3000); // Wait for page load

            // Check for authentication walls or security challenges
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (currentUrl.contains("authwall") || currentUrl.contains("login") || currentUrl.contains("signup")) {
                throw new RuntimeException("LinkedIn requires authentication - cannot access company page: " + currentUrl);
            }

            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("captcha") || pageSource.contains("security check")) {
                throw new RuntimeException("CAPTCHA or security challenge detected");
            }

            // Phase 5: Content extraction using dedicated utility
            logger.info("Phase 5: Extracting LinkedIn content");
            ContentExtractionUtil.LinkedInContent extractedContent = contentExtractionUtil.extractLinkedInContent(driver, companyName, linkedinCompanyId);
            
            String companyDescription = extractedContent.description;
            List<String> posts = extractedContent.posts;

            logger.info("✅ Extracted company description: {} chars, posts: {}", 
                       companyDescription != null ? companyDescription.length() : 0, posts.size());

            // Phase 6: AI analysis orchestration using dedicated utility
            logger.info("Phase 6: Orchestrating comprehensive AI analysis");
            String analysis = analysisOrchestrationUtil.orchestrateLinkedInAnalysis(companyName, extractedContent.companyTitle, companyDescription, posts);

            long totalDuration = System.currentTimeMillis() - analysisStartTime;
            logger.info("====== MODULAR LINKEDIN ANALYSIS COMPLETED FOR '{}' in {}ms ======", companyName, totalDuration);

            return analysis;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - analysisStartTime;
            logger.error("❌ Modular LinkedIn analysis failed for '{}' after {}ms", companyName, totalDuration);
            logger.error("Error: {}", e.getMessage(), e);

            // Fallback using TavilyFallbackService
            try {
                logger.warn("🔄 Attempting Tavily fallback for LinkedIn analysis...");
                String fallbackResult = tavilyFallbackService.getLinkedInAnalysisFallback(companyName);
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
}