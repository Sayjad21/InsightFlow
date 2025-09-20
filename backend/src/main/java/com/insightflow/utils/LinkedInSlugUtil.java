package com.insightflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility service for generating and validating LinkedIn company slugs.
 * Extracted from the original monolithic ScrapingService for better modularity.
 * 
 * This service provides comprehensive slug generation with multiple fallback
 * strategies:
 * - Hardcoded mappings for common companies
 * - Dynamic search using TavilyUtil
 * - Enhanced validation with Chrome and Jsoup
 * - Fallback slug generation when all else fails
 */
@Component
public class LinkedInSlugUtil {

    private static final Logger logger = LoggerFactory.getLogger(LinkedInSlugUtil.class);

    @Autowired
    private TavilyUtil tavilyUtil;

    private final Random random = new Random();
    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (X11; Linux x86_64; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:118.0) Gecko/20100101 Firefox/118.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:118.0) Gecko/20100101 Firefox/118.0"
    };

    /**
     * Helper class to store LinkedIn company candidate information
     */
    public static class CompanyCandidate {
        public final String slug;
        public final String url;
        public final String title;
        public final String content;
        public int followerCount = -1;
        public double relevanceScore = 0.0;
        public String selectionMethod = "unknown";

        public CompanyCandidate(String slug, String url, String title, String content) {
            this.slug = slug;
            this.url = url;
            this.title = title != null ? title : "";
            this.content = content != null ? content : "";
        }
    }

    /**
     * Main method to get LinkedIn company slug with comprehensive fallback
     * strategies.
     * 
     * @param companyName The name of the company
     * @return LinkedIn company slug or fallback slug if not found
     */
    public String getLinkedInCompanySlug(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            logger.warn("Company name is null or empty, returning empty slug");
            return "";
        }

        logger.info("=== GETTING LINKEDIN SLUG FOR: '{}' ===", companyName);

        String normalizedName = companyName.toLowerCase().trim();

        // Strategy 1: Check hardcoded mappings for common companies
        String hardcodedSlug = getHardcodedSlug(normalizedName);
        if (hardcodedSlug != null) {
            logger.info("✓ Using hardcoded slug: {} -> {}", companyName, hardcodedSlug);
            return hardcodedSlug;
        }

        // Strategy 2: Dynamic search using TavilyUtil with multiple fallback strategies
        try {
            logger.info("Attempting dynamic LinkedIn search for: {}", companyName);

            // Use enhanced LinkedIn search from TavilyUtil
            List<Map<String, Object>> searchResults = tavilyUtil.searchLinkedInCompany(companyName);
            logger.info("Enhanced LinkedIn search returned {} results for '{}'", searchResults.size(), companyName);

            // If no results, try deep search with variations
            if (searchResults.isEmpty()) {
                logger.warn("Standard LinkedIn search failed for {}, trying deep search with variations...",
                        companyName);
                searchResults = tavilyUtil.deepSearchLinkedInCompany(companyName, true);
                logger.info("Deep search with variations returned {} results", searchResults.size());
            }

            if (searchResults.isEmpty()) {
                logger.warn("All enhanced search strategies failed for {}; using normalized name fallback",
                        companyName);
                String fallbackSlug = generateEnhancedFallbackSlug(companyName);
                logger.info("Generated fallback slug: {} -> {}", companyName, fallbackSlug);
                return fallbackSlug;
            }

            // Extract all potential LinkedIn company slugs with enhanced validation
            List<CompanyCandidate> candidates = extractCandidatesFromSearchResults(searchResults, companyName);

            if (candidates.isEmpty()) {
                logger.warn("No valid LinkedIn company URLs found after filtering; trying HEAD request fallback...");

                // Try direct URL validation as fallback
                String directSlug = findValidLinkedInSlug(companyName);
                if (directSlug != null) {
                    logger.info("✓ Found valid LinkedIn slug via direct URL validation: {} -> {}", companyName,
                            directSlug);
                    return directSlug;
                }

                logger.warn("Direct URL validation also failed, using fallback slug generation...");
                return generateFallbackSlug(companyName, searchResults);
            }

            // Enhanced candidate selection with multiple strategies
            return selectBestLinkedInCandidate(companyName, candidates);

        } catch (Exception e) {
            logger.error("Failed to dynamically find LinkedIn slug for {}: {}", companyName, e.getMessage(), e);
            // Enhanced fallback to normalized name with variations
            return generateEnhancedFallbackSlug(companyName);
        }
    }

    /**
     * Gets hardcoded slug mappings for common companies
     */
    private String getHardcodedSlug(String normalizedName) {
        switch (normalizedName) {
            case "tesla":
                return "tesla-motors";
            case "meta":
            case "facebook":
                return "meta";
            case "alphabet":
                return "google";
            case "x":
            case "twitter":
                return "twitter";
            case "openai":
                return "openai";
            case "microsoft":
                return "microsoft";
            case "apple":
                return "apple";
            case "amazon":
                return "amazon";
            case "netflix":
                return "netflix";
            // case "deepseek":
            // return "deepseek-ai";
            // case "anthropic":
            // return "anthropic-research";
            default:
                return null;
        }
    }

    /**
     * Extracts company candidates from search results
     */
    private List<CompanyCandidate> extractCandidatesFromSearchResults(List<Map<String, Object>> searchResults,
            String companyName) {
        List<CompanyCandidate> candidates = new ArrayList<>();
        logger.info("Processing {} search results for candidate extraction:", searchResults.size());

        for (int i = 0; i < searchResults.size(); i++) {
            Map<String, Object> result = searchResults.get(i);
            String url = (String) result.get("url");
            String title = (String) result.get("title");
            String content = (String) result.get("content");

            logger.info("Search result #{}: URL={}, Title={}", i + 1, url, title);

            if (url != null && url.contains("linkedin.com/company/")) {
                // Extract slug after /company/
                String slug = extractLinkedInSlugFromUrl(url);
                logger.info("Extracted slug from URL: '{}' -> '{}'", url, slug);

                if (!slug.isEmpty() && isValidLinkedInSlug(slug, companyName)) {
                    candidates.add(new CompanyCandidate(slug, url, title, content));
                    logger.info("✓ Valid LinkedIn candidate found: {} -> {} ({})", companyName, slug, title);
                } else {
                    logger.warn("✗ Invalid or irrelevant LinkedIn slug rejected: '{}' (from URL: {})", slug, url);
                }
            } else {
                logger.warn("✗ Search result does not contain LinkedIn company URL: {}", url);
            }
        }

        return candidates;
    }

    /**
     * Enhanced method for extracting LinkedIn slug from URL with better parsing
     */
    public String extractLinkedInSlugFromUrl(String url) {
        logger.debug("Extracting LinkedIn slug from URL: {}", url);
        try {
            if (url == null || !url.contains("linkedin.com/company/")) {
                logger.warn("URL extraction failed: URL is null or doesn't contain linkedin.com/company/");
                return "";
            }

            // Extract slug after /company/
            String slug = url.substring(url.indexOf("/company/") + 9);
            // Remove trailing parts like query parameters, sub-paths, or fragments
            slug = slug.replaceAll("[/?#].*", "");
            // Remove any trailing slashes
            slug = slug.replaceAll("/$", "");

            logger.debug("✓ Successfully extracted slug '{}' from URL '{}'", slug, url);
            return slug;
        } catch (Exception e) {
            logger.error("Failed to extract slug from URL '{}': {}", url, e.getMessage());
            return "";
        }
    }

    /**
     * Validates if a LinkedIn slug is relevant to the company name
     */
    public boolean isValidLinkedInSlug(String slug, String companyName) {
        logger.debug("Validating LinkedIn slug: '{}' for company: '{}'", slug, companyName);

        if (slug == null || slug.trim().isEmpty()) {
            logger.warn("Rejection reason: Slug is null or empty");
            return false;
        }

        String lowerSlug = slug.toLowerCase();
        String lowerCompany = companyName.toLowerCase();

        // Skip obviously invalid slugs
        if (lowerSlug.length() < 2 || lowerSlug.matches("^\\d+$")) {
            logger.warn("Rejection reason: Slug too short or numeric only: '{}'", slug);
            return false;
        }

        // Skip generic or common LinkedIn paths
        String[] invalidSlugs = { "home", "login", "company", "about", "help", "search", "feed", "messaging" };
        for (String invalid : invalidSlugs) {
            if (lowerSlug.equals(invalid)) {
                logger.warn("Rejection reason: Generic LinkedIn path: '{}'", slug);
                return false;
            }
        }

        // Skip very long slugs (likely contain query parameters or paths)
        if (slug.length() > 50) {
            logger.warn("Rejection reason: Slug too long ({}): '{}'", slug.length(), slug);
            return false;
        }

        // General acceptance: if slug contains part of company name or vice versa
        if (lowerSlug.contains(lowerCompany) || lowerCompany.contains(lowerSlug)) {
            logger.debug("✓ Slug contains company name or vice versa: '{}' <-> '{}'", slug, companyName);
            return true;
        }

        logger.debug("✓ Slug '{}' passed basic validation for company '{}'", slug, companyName);
        return true;
    }

    /**
     * Enhanced candidate selection with comprehensive fallback strategies
     */
    public String selectBestLinkedInCandidate(String companyName, List<CompanyCandidate> candidates) {
        logger.info("=== SELECTING BEST LINKEDIN CANDIDATE FOR: {} ===", companyName);
        logger.info("Total candidates to evaluate: {}", candidates.size());

        // If only one candidate, return it but with validation
        if (candidates.size() == 1) {
            CompanyCandidate single = candidates.get(0);
            logger.info("Single candidate found: {} ({})", single.slug, single.title);

            // Quick relevance check for single candidate
            double relevance = calculateRelevanceScore(companyName, single);
            if (relevance > 30.0) { // Lower threshold for single candidate
                logger.info("✓ Single candidate accepted with relevance: {}", relevance);
                return single.slug;
            } else {
                logger.warn("⚠ Single candidate has low relevance ({}), but proceeding anyway", relevance);
                return single.slug;
            }
        }

        // Multiple candidates - comprehensive selection
        logger.info("Multiple candidates found, starting comprehensive evaluation...");

        // Calculate relevance scores for all candidates
        for (int i = 0; i < candidates.size(); i++) {
            CompanyCandidate candidate = candidates.get(i);
            candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);
            logger.info("Candidate {}: {} -> Relevance: {:.2f} ({})",
                    i + 1, candidate.slug, candidate.relevanceScore, candidate.title);
        }

        // Strategy 1: Try Chrome validation (if available)
        logger.info("Attempting Chrome validation...");
        CompanyCandidate chromeValidated = tryChromeValidation(companyName, candidates);
        if (chromeValidated != null) {
            chromeValidated.selectionMethod = "chrome-validation";
            logger.info("✓ Chrome validation successful: {} (followers: {})",
                    chromeValidated.slug, chromeValidated.followerCount);
            return chromeValidated.slug;
        }

        // Strategy 2: Try Jsoup validation (lightweight alternative)
        logger.info("Chrome validation failed, attempting Jsoup validation...");
        CompanyCandidate jsoupValidated = tryJsoupValidation(companyName, candidates);
        if (jsoupValidated != null) {
            jsoupValidated.selectionMethod = "jsoup-validation";
            logger.info("✓ Jsoup validation successful: {} (followers: {})",
                    jsoupValidated.slug, jsoupValidated.followerCount);
            return jsoupValidated.slug;
        }

        // Strategy 3: Enhanced heuristic selection
        logger.info("Validation methods failed, using enhanced heuristic analysis...");
        CompanyCandidate heuristicBest = selectByEnhancedHeuristics(companyName, candidates);
        heuristicBest.selectionMethod = "enhanced-heuristics";
        logger.info("✓ Heuristic selection completed: {} (relevance: {:.2f})",
                heuristicBest.slug, heuristicBest.relevanceScore);

        return heuristicBest.slug;
    }

    /**
     * Calculates relevance score between company name and LinkedIn candidate
     */
    private double calculateRelevanceScore(String companyName, CompanyCandidate candidate) {
        double score = 0.0;
        String lowerCompanyName = companyName.toLowerCase();
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();

        // Exact match in title gets highest score
        if (lowerTitle.equals(lowerCompanyName)) {
            score += 100.0;
        } else if (lowerTitle.contains(lowerCompanyName)) {
            score += 50.0;
        }

        // Partial matches in title
        String[] companyWords = lowerCompanyName.split("\\s+");
        for (String word : companyWords) {
            if (word.length() > 2 && lowerTitle.contains(word)) {
                score += 10.0;
            }
        }

        // Content matches (lower weight)
        if (lowerContent.contains(lowerCompanyName)) {
            score += 20.0;
        }

        // Known corporate suffixes
        if (lowerTitle.contains(" inc") || lowerTitle.contains(" corp") ||
                lowerTitle.contains(" ltd") || lowerTitle.contains(" llc")) {
            score += 5.0;
        }

        return score;
    }

    /**
     * Enhanced heuristics with multiple selection strategies
     */
    private CompanyCandidate selectByEnhancedHeuristics(String companyName, List<CompanyCandidate> candidates) {
        String lowerCompanyName = companyName.toLowerCase();
        logger.info("Running enhanced heuristic analysis for: {}", companyName);

        // Strategy 1: Look for exact matches or very close variants
        for (CompanyCandidate candidate : candidates) {
            String lowerSlug = candidate.slug.toLowerCase();

            // Perfect slug match
            if (lowerSlug.equals(lowerCompanyName)) {
                logger.info("✓ Perfect slug match found: {}", candidate.slug);
                return candidate;
            }

            // HQ variant (common pattern)
            if (lowerSlug.equals(lowerCompanyName + "hq") || lowerSlug.equals(lowerCompanyName + "-hq")) {
                logger.info("✓ HQ variant match found: {}", candidate.slug);
                return candidate;
            }

            // Official/Inc variants
            if (lowerSlug.equals(lowerCompanyName + "inc") || lowerSlug.equals(lowerCompanyName + "-inc") ||
                    lowerSlug.equals(lowerCompanyName + "official")
                    || lowerSlug.equals(lowerCompanyName + "-official")) {
                logger.info("✓ Corporate variant match found: {}", candidate.slug);
                return candidate;
            }
        }

        // Strategy 2: Prioritize by relevance score and filter out distributors
        CompanyCandidate bestNonDistributor = candidates.stream()
                .filter(c -> !isLikelyDistributor(c))
                .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                .orElse(null);

        // Strategy 3: If no non-distributor found, use highest relevance score
        CompanyCandidate bestOverall = candidates.stream()
                .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                .orElse(candidates.get(0));

        CompanyCandidate finalChoice = bestNonDistributor != null ? bestNonDistributor : bestOverall;

        logger.info("Enhanced heuristic selected: {} (relevance: {:.2f}, is_distributor: {})",
                finalChoice.slug, finalChoice.relevanceScore, isLikelyDistributor(finalChoice));

        return finalChoice;
    }

    /**
     * Checks if a candidate is likely a distributor/reseller rather than the main
     * company
     */
    private boolean isLikelyDistributor(CompanyCandidate candidate) {
        String lowerSlug = candidate.slug.toLowerCase();
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();

        // Distributors often have these keywords
        return lowerSlug.contains("distribution") ||
                lowerSlug.contains("distributor") ||
                lowerSlug.contains("dealer") ||
                lowerSlug.contains("reseller") ||
                lowerTitle.contains("distribution") ||
                lowerTitle.contains("distributor") ||
                lowerTitle.contains("dealer") ||
                lowerTitle.contains("reseller") ||
                lowerContent.contains("authorized dealer") ||
                lowerContent.contains("official distributor");
    }

    /**
     * Attempts Chrome-based validation (with better error handling)
     */
    private CompanyCandidate tryChromeValidation(String companyName, List<CompanyCandidate> candidates) {
        WebDriver driver = null;
        String tempUserDataDir = null;

        try {
            // Setup lightweight Chrome instance for validation
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            tempUserDataDir = System.getProperty("java.io.tmpdir") + "chrome_validate_" +
                    System.currentTimeMillis() + "_" + random.nextInt(10000);

            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-data-dir=" + tempUserDataDir);
            options.addArguments("--disable-notifications");
            options.addArguments("--timeout=10000");
            options.addArguments("user-agent=" + userAgents[random.nextInt(userAgents.length)]);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // Check each candidate
            for (CompanyCandidate candidate : candidates) {
                try {
                    logger.info("Validating candidate: {} for company: {}", candidate.slug, companyName);

                    // Navigate to the LinkedIn company page (public view)
                    String publicUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    driver.get(publicUrl);
                    Thread.sleep(2000); // Wait for page load

                    // Calculate relevance score based on title/content match
                    candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);

                    // Try to extract follower count from public page
                    candidate.followerCount = extractFollowerCount(driver);

                    logger.info("Candidate {} - Followers: {}, Relevance: {}",
                            candidate.slug, candidate.followerCount, candidate.relevanceScore);

                } catch (Exception e) {
                    logger.warn("Failed to validate candidate {}: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1; // Mark as failed
                }
            }

            // Select best candidate based on follower count and relevance
            return candidates.stream()
                    .filter(c -> c.followerCount > 0) // Only valid candidates
                    .max((c1, c2) -> {
                        // Primary sort: follower count (higher is better)
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        // Secondary sort: relevance score (higher is better)
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(candidates.get(0)); // Fallback to first if none have follower counts

        } catch (Exception e) {
            logger.error("Error during Chrome candidate validation: {}", e.getMessage());
            return null; // Return null to indicate Chrome validation failed
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("Error closing validation driver: {}", e.getMessage());
                }
            }

            // Clean up temp directory
            if (tempUserDataDir != null) {
                try {
                    Files.walk(Paths.get(tempUserDataDir))
                            .sorted(Comparator.reverseOrder())
                            .map(java.nio.file.Path::toFile)
                            .forEach(java.io.File::delete);
                } catch (Exception e) {
                    logger.warn("Failed to clean up temp directory: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Attempts to validate company candidates using Jsoup (HTTP + HTML parsing)
     * This is lighter weight than Chrome validation but still gets actual follower
     * counts
     */
    private CompanyCandidate tryJsoupValidation(String companyName, List<CompanyCandidate> candidates) {
        try {
            logger.info("Starting Jsoup validation for {} with {} candidates", companyName, candidates.size());

            // Try to extract follower counts using Jsoup
            for (CompanyCandidate candidate : candidates) {
                try {
                    String linkedinUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    logger.debug("Attempting Jsoup follower extraction for: {}", linkedinUrl);

                    // Use Jsoup to fetch and parse the LinkedIn page
                    Document doc = Jsoup.connect(linkedinUrl)
                            .userAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(10000)
                            .followRedirects(true)
                            .get();

                    // Try multiple strategies to extract follower count
                    int followerCount = extractFollowerCountFromHtml(doc);

                    if (followerCount > 0) {
                        candidate.followerCount = followerCount;
                        logger.info("Jsoup extracted {} followers for candidate: {} ({})",
                                followerCount, candidate.title, candidate.slug);
                    } else {
                        logger.warn("No followers found via Jsoup for candidate: {} ({})",
                                candidate.title, candidate.slug);
                        candidate.followerCount = -1;
                    }

                } catch (Exception e) {
                    logger.warn("Failed to validate candidate {} via Jsoup: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1; // Mark as failed
                }
            }

            // Select best candidate based on follower count and relevance
            return candidates.stream()
                    .filter(c -> c.followerCount > 0) // Only valid candidates
                    .max((c1, c2) -> {
                        // Primary sort: follower count (higher is better)
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        // Secondary sort: relevance score (higher is better)
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(null); // Return null if no candidates have valid follower counts

        } catch (Exception e) {
            logger.warn("Jsoup validation failed for {}: {}", companyName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts follower count from LinkedIn company page HTML using multiple
     * strategies
     */
    private int extractFollowerCountFromHtml(Document doc) {
        try {
            // Strategy 1: Look for follower count in meta tags
            Elements metaTags = doc.select("meta[name*=follower], meta[property*=follower], meta[content*=follower]");
            for (Element meta : metaTags) {
                String content = meta.attr("content");
                int count = parseFollowerCount(content);
                if (count > 0) {
                    return count;
                }
            }

            // Strategy 2: Look for follower text in page elements
            Elements elements = doc.select("*:contains(follower)");
            for (Element element : elements) {
                String text = element.text();
                int count = parseFollowerCount(text);
                if (count > 0) {
                    return count;
                }
            }

            // Strategy 3: Parse entire page text as fallback
            String pageText = doc.text().toLowerCase();
            return parseFollowerCountFromPage(pageText);

        } catch (Exception e) {
            logger.debug("Failed to extract follower count from HTML: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Extracts follower count from LinkedIn public company page using WebDriver
     */
    private int extractFollowerCount(WebDriver driver) {
        try {
            // Fallback: search entire page for follower patterns
            String pageText = driver.getPageSource().toLowerCase();
            return parseFollowerCountFromPage(pageText);

        } catch (Exception e) {
            logger.debug("Failed to extract follower count: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Parses follower count from text
     */
    private int parseFollowerCount(String text) {
        try {
            // Look for patterns like "1,234 followers", "1.2K followers", "1.2M followers"
            Pattern pattern = Pattern.compile("([\\d,\\.]+)\\s*([kmb]?)\\s*followers?", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String numberStr = matcher.group(1).replace(",", "").replace(".", "");
                String multiplier = matcher.group(2).toLowerCase();

                int baseNumber = Integer.parseInt(numberStr);

                switch (multiplier) {
                    case "k":
                        return baseNumber * 1000;
                    case "m":
                        return baseNumber * 1000000;
                    case "b":
                        return baseNumber * 1000000000;
                    default:
                        return baseNumber;
                }
            }
        } catch (Exception e) {
            // Continue to next strategy
        }
        return -1;
    }

    /**
     * Parses follower count from entire page content
     */
    private int parseFollowerCountFromPage(String pageContent) {
        try {
            Pattern pattern = Pattern.compile("([\\d,]+)\\s+followers?", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(pageContent);

            int maxFollowers = -1;
            while (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "");
                    int followers = Integer.parseInt(numberStr);
                    if (followers > maxFollowers) {
                        maxFollowers = followers;
                    }
                } catch (NumberFormatException e) {
                    // Continue searching
                }
            }

            return maxFollowers;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Validates if a LinkedIn company URL exists using HEAD request
     */
    public boolean validateLinkedInCompanyExists(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        String url = "https://www.linkedin.com/company/" + slug;
        logger.debug("Validating LinkedIn company URL: {}", url);

        try {
            java.net.URI uri = java.net.URI.create(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5 second timeout
            connection.setReadTimeout(5000);

            // Set user agent to avoid blocking
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            int responseCode = connection.getResponseCode();
            logger.debug("HEAD request to {} returned status code: {}", url, responseCode);

            return responseCode == 200;

        } catch (Exception e) {
            logger.debug("Failed to validate LinkedIn URL {}: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Generates and tests LinkedIn slug variations for a company
     */
    public String findValidLinkedInSlug(String companyName) {
        logger.info("Finding valid LinkedIn slug for company: {}", companyName);

        List<String> slugCandidates = new ArrayList<>();
        String lower = companyName.toLowerCase().trim();

        // Generate basic slug variations
        String basicSlug = lower.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        slugCandidates.add(basicSlug);

        // Corporate suffixes
        String[] suffixes = { "-inc", "-corp", "-corporation", "-ltd", "-llc", "-technologies", "-labs" };
        for (String suffix : suffixes) {
            slugCandidates.add(basicSlug + suffix);
        }

        // Remove existing suffixes variations
        String[] removable = { "-inc", "-corp", "-corporation", "-ltd", "-llc", "-company", "-co" };
        for (String remove : removable) {
            if (basicSlug.endsWith(remove)) {
                String clean = basicSlug.substring(0, basicSlug.length() - remove.length());
                slugCandidates.add(clean);
            }
        }

        // Test each candidate
        for (String candidate : slugCandidates) {
            if (candidate != null && !candidate.trim().isEmpty()) {
                logger.debug("Testing LinkedIn slug candidate: {}", candidate);
                if (validateLinkedInCompanyExists(candidate)) {
                    logger.info("✓ Found valid LinkedIn slug for {}: {}", companyName, candidate);
                    return candidate;
                }
            }
        }

        logger.warn("No valid LinkedIn slug found for company: {}", companyName);
        return null;
    }

    /**
     * Generates fallback slug when no valid candidates found but search results
     * exist
     */
    public String generateFallbackSlug(String companyName, List<Map<String, Object>> searchResults) {
        logger.info("Attempting to generate fallback slug for '{}' from {} search results", companyName,
                searchResults.size());

        // Look for any LinkedIn URLs in search results and try to extract reasonable
        // slugs
        for (Map<String, Object> result : searchResults) {
            String url = (String) result.get("url");
            if (url != null && url.contains("linkedin.com")) {
                // Try to extract any company-related slug from LinkedIn URLs
                if (url.contains("/company/")) {
                    String slug = extractLinkedInSlugFromUrl(url);
                    if (!slug.isEmpty() && slug.length() > 2) {
                        logger.info("Generated fallback slug from search results: {}", slug);
                        return slug;
                    }
                }
            }
        }

        // Final fallback: generate from company name
        String fallbackSlug = companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special chars except spaces and hyphens
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Collapse multiple hyphens
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

        logger.info("Generated normalized fallback slug: {} -> {}", companyName, fallbackSlug);
        return fallbackSlug;
    }

    /**
     * Enhanced fallback slug generation with multiple variations
     */
    public String generateEnhancedFallbackSlug(String companyName) {
        logger.info("Generating enhanced fallback slug for: {}", companyName);

        // Try multiple common LinkedIn slug patterns
        String normalized = companyName.toLowerCase().replaceAll("[^a-z0-9-]", "");

        // Pattern 1: Simple normalization (current approach)
        String pattern1 = normalized;

        // Pattern 2: Replace spaces with hyphens
        String pattern2 = companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special chars but keep spaces
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Collapse multiple hyphens
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

        // Pattern 3: Common corporate suffixes handled
        String pattern3 = companyName.toLowerCase()
                .replaceAll("\\b(inc|corp|corporation|ltd|llc|company)\\b", "") // Remove corporate suffixes
                .replaceAll("[^a-z0-9\\s]", "") // Remove special chars
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Collapse multiple hyphens
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

        // Choose the best pattern (prefer shorter, cleaner ones)
        String[] patterns = { pattern1, pattern2, pattern3 };
        String bestPattern = pattern1; // Default

        for (String pattern : patterns) {
            if (!pattern.isEmpty() && pattern.length() > 1 && pattern.length() < bestPattern.length()) {
                bestPattern = pattern;
            }
        }

        logger.info("Enhanced fallback slug selection: {} -> {}", companyName, bestPattern);
        return bestPattern;
    }
}