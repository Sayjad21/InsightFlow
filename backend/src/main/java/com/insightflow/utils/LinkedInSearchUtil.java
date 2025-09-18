package com.insightflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Utility class for LinkedIn company search, validation, and candidate selection.
 * Handles company name to LinkedIn profile matching with multiple strategies.
 */
@Component
public class LinkedInSearchUtil {

    private static final Logger logger = LoggerFactory.getLogger(LinkedInSearchUtil.class);

    @Autowired
    private TavilyUtil tavilyUtil;

    private final Random random = new Random();

    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
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
     * Main method to get LinkedIn company ID for a given company name.
     * Uses multiple strategies including hardcoded mappings, search APIs, and fallback generation.
     * 
     * @param companyName The company name to search for
     * @return LinkedIn company slug/ID
     */
    public String getLinkedInCompanyId(String companyName) {
        String normalizedName = companyName.toLowerCase().trim();

        // Hardcoded fallback for common companies
        String hardcodedSlug = getHardcodedSlug(normalizedName);
        if (hardcodedSlug != null) {
            logger.info("Using hardcoded slug for {}: {}", companyName, hardcodedSlug);
            return hardcodedSlug;
        }

        // Dynamic search using multiple strategies
        try {
            logger.info("=== STARTING LINKEDIN COMPANY SEARCH FOR: {} ===", companyName);

            // Strategy 1: Enhanced LinkedIn search from TavilyUtil
            List<Map<String, Object>> searchResults = tavilyUtil.searchLinkedInCompany(companyName);
            logger.info("Enhanced LinkedIn search returned {} results for '{}'", searchResults.size(), companyName);

            // Strategy 2: Deep search with variations if no results
            if (searchResults.isEmpty()) {
                logger.warn("Standard LinkedIn search failed for {}, trying deep search with variations...", companyName);
                searchResults = tavilyUtil.deepSearchLinkedInCompany(companyName, true);
                logger.info("Deep search with variations returned {} results", searchResults.size());
            }

            if (searchResults.isEmpty()) {
                logger.warn("All enhanced search strategies failed for {}; using normalized name fallback", companyName);
                String fallbackSlug = generateEnhancedFallbackSlug(companyName);
                logger.info("Generated fallback slug: {} -> {}", companyName, fallbackSlug);
                return fallbackSlug;
            }

            // Extract and validate LinkedIn company candidates
            List<CompanyCandidate> candidates = extractCandidatesFromResults(companyName, searchResults);

            if (candidates.isEmpty()) {
                logger.warn("No valid LinkedIn company URLs found after filtering; trying direct URL validation...");
                String directSlug = findValidLinkedInSlug(companyName);
                if (directSlug != null) {
                    logger.info("✓ Found valid LinkedIn slug via direct URL validation: {} -> {}", companyName, directSlug);
                    return directSlug;
                }

                logger.warn("Direct URL validation also failed, using fallback slug generation...");
                return generateFallbackSlug(companyName, searchResults);
            }

            // Select best candidate using comprehensive strategies
            return selectBestLinkedInCandidate(companyName, candidates);

        } catch (Exception e) {
            logger.error("Failed to dynamically find LinkedIn ID for {}: {}", companyName, e.getMessage(), e);
            return generateEnhancedFallbackSlug(companyName);
        }
    }

    /**
     * Gets hardcoded LinkedIn slug for well-known companies
     */
    private String getHardcodedSlug(String normalizedName) {
        switch (normalizedName) {
            case "tesla": return "tesla-motors";
            case "meta":
            case "facebook": return "meta";
            case "alphabet": return "google";
            case "x":
            case "twitter": return "twitter";
            case "openai": return "openai";
            case "microsoft": return "microsoft";
            case "apple": return "apple";
            case "amazon": return "amazon";
            case "netflix": return "netflix";
            default: return null;
        }
    }

    /**
     * Extracts LinkedIn company candidates from search results
     */
    private List<CompanyCandidate> extractCandidatesFromResults(String companyName, List<Map<String, Object>> searchResults) {
        List<CompanyCandidate> candidates = new ArrayList<>();
        logger.info("Processing {} search results for candidate extraction:", searchResults.size());

        for (int i = 0; i < searchResults.size(); i++) {
            Map<String, Object> result = searchResults.get(i);
            String url = (String) result.get("url");
            String title = (String) result.get("title");
            String content = (String) result.get("content");

            logger.info("Search result #{}: URL={}, Title={}", i + 1, url, title);

            if (url != null && url.contains("linkedin.com/company/")) {
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
     * Extracts LinkedIn slug from URL with enhanced parsing
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
        String[] invalidSlugs = {"home", "login", "company", "about", "help", "search", "feed", "messaging"};
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
     * Selects the best LinkedIn candidate using multiple strategies
     */
    private String selectBestLinkedInCandidate(String companyName, List<CompanyCandidate> candidates) {
        logger.info("=== SELECTING BEST LINKEDIN CANDIDATE FOR: {} ===", companyName);
        logger.info("Total candidates to evaluate: {}", candidates.size());

        // Single candidate handling
        if (candidates.size() == 1) {
            CompanyCandidate single = candidates.get(0);
            logger.info("Single candidate found: {} ({})", single.slug, single.title);

            double relevance = calculateRelevanceScore(companyName, single);
            if (relevance > 30.0) {
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

        // Strategy 1: Try Chrome validation
        logger.info("Attempting Chrome validation...");
        CompanyCandidate chromeValidated = tryChromeValidation(companyName, candidates);
        if (chromeValidated != null) {
            chromeValidated.selectionMethod = "chrome-validation";
            logger.info("✓ Chrome validation successful: {} (followers: {})",
                    chromeValidated.slug, chromeValidated.followerCount);
            return chromeValidated.slug;
        }

        // Strategy 2: Try Jsoup validation
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
     * Attempts Chrome-based validation for candidates
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

                    String publicUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    driver.get(publicUrl);
                    Thread.sleep(2000);

                    candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);
                    candidate.followerCount = extractFollowerCount(driver);

                    logger.info("Candidate {} - Followers: {}, Relevance: {}",
                            candidate.slug, candidate.followerCount, candidate.relevanceScore);

                } catch (Exception e) {
                    logger.warn("Failed to validate candidate {}: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1;
                }
            }

            // Select best candidate based on follower count and relevance
            return candidates.stream()
                    .filter(c -> c.followerCount > 0)
                    .max((c1, c2) -> {
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(candidates.get(0));

        } catch (Exception e) {
            logger.warn("Chrome validation failed: {}", e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("Error closing Chrome driver: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Attempts Jsoup-based validation for candidates
     */
    private CompanyCandidate tryJsoupValidation(String companyName, List<CompanyCandidate> candidates) {
        try {
            logger.info("Starting Jsoup validation for {} candidates", candidates.size());

            for (CompanyCandidate candidate : candidates) {
                try {
                    String publicUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    logger.info("Jsoup validating: {}", publicUrl);

                    Document doc = Jsoup.connect(publicUrl)
                            .userAgent(userAgents[random.nextInt(userAgents.length)])
                            .timeout(10000)
                            .get();

                    candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);
                    candidate.followerCount = extractFollowerCountFromHtml(doc);

                    logger.info("Jsoup candidate {} - Followers: {}, Relevance: {}",
                            candidate.slug, candidate.followerCount, candidate.relevanceScore);

                } catch (Exception e) {
                    logger.warn("Jsoup validation failed for candidate {}: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1;
                }
            }

            return candidates.stream()
                    .filter(c -> c.followerCount > 0)
                    .max((c1, c2) -> {
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(null);

        } catch (Exception e) {
            logger.warn("Jsoup validation completely failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts follower count from Selenium WebDriver
     */
    private int extractFollowerCount(WebDriver driver) {
        try {
            String pageSource = driver.getPageSource();
            return parseFollowerCountFromPage(pageSource);
        } catch (Exception e) {
            logger.warn("Failed to extract follower count with WebDriver: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Extracts follower count from Jsoup HTML document
     */
    private int extractFollowerCountFromHtml(Document doc) {
        try {
            // Try multiple selectors for follower count
            Elements followerElements = doc.select("div:containsOwn(followers)");
            if (!followerElements.isEmpty()) {
                String followerText = followerElements.first().text();
                return parseFollowerCount(followerText);
            }

            // Try alternative patterns
            String pageText = doc.text();
            return parseFollowerCountFromPage(pageText);

        } catch (Exception e) {
            logger.warn("Failed to extract follower count from HTML: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Parses follower count from text
     */
    private int parseFollowerCount(String text) {
        if (text == null) return -1;

        try {
            // Look for patterns like "1,234 followers" or "1K followers"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([\\d,]+(?:\\.\\d+)?\\s*[KMB]?)\\s+followers?", 
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String numberStr = matcher.group(1).replaceAll("[,\\s]", "");
                
                if (numberStr.endsWith("K")) {
                    return (int) (Double.parseDouble(numberStr.substring(0, numberStr.length() - 1)) * 1000);
                } else if (numberStr.endsWith("M")) {
                    return (int) (Double.parseDouble(numberStr.substring(0, numberStr.length() - 1)) * 1000000);
                } else if (numberStr.endsWith("B")) {
                    return (int) (Double.parseDouble(numberStr.substring(0, numberStr.length() - 1)) * 1000000000);
                } else {
                    return Integer.parseInt(numberStr);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing follower count from text '{}': {}", text, e.getMessage());
        }

        return -1;
    }

    /**
     * Parses follower count from page content
     */
    private int parseFollowerCountFromPage(String pageContent) {
        if (pageContent == null) return -1;

        // Try multiple patterns to find follower count
        String[] patterns = {
            "([\\d,]+(?:\\.\\d+)?\\s*[KMB]?)\\s+followers?",
            "\"followerCount\"\\s*:\\s*([\\d,]+)",
            "([\\d,]+)\\s+followers?"
        };

        for (String patternStr : patterns) {
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr, 
                        java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher matcher = pattern.matcher(pageContent);

                if (matcher.find()) {
                    return parseFollowerCount(matcher.group(1) + " followers");
                }
            } catch (Exception e) {
                logger.debug("Pattern '{}' failed: {}", patternStr, e.getMessage());
            }
        }

        return -1;
    }

    /**
     * Enhanced heuristic selection with multiple strategies
     */
    private CompanyCandidate selectByEnhancedHeuristics(String companyName, List<CompanyCandidate> candidates) {
        String lowerCompanyName = companyName.toLowerCase();
        logger.info("Running enhanced heuristic analysis for: {}", companyName);

        // Strategy 1: Look for exact matches or very close variants
        for (CompanyCandidate candidate : candidates) {
            String lowerSlug = candidate.slug.toLowerCase();

            if (lowerSlug.equals(lowerCompanyName)) {
                logger.info("✓ Perfect slug match found: {}", candidate.slug);
                return candidate;
            }

            if (lowerSlug.equals(lowerCompanyName + "hq") || lowerSlug.equals(lowerCompanyName + "-hq")) {
                logger.info("✓ HQ variant match found: {}", candidate.slug);
                return candidate;
            }

            if (lowerSlug.equals(lowerCompanyName + "inc") || lowerSlug.equals(lowerCompanyName + "-inc") ||
                    lowerSlug.equals(lowerCompanyName + "official") || lowerSlug.equals(lowerCompanyName + "-official")) {
                logger.info("✓ Corporate variant match found: {}", candidate.slug);
                return candidate;
            }
        }

        // Strategy 2: Prioritize by relevance score and filter out distributors
        CompanyCandidate bestNonDistributor = candidates.stream()
                .filter(c -> !isLikelyDistributor(c))
                .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                .orElse(null);

        CompanyCandidate bestOverall = candidates.stream()
                .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                .orElse(candidates.get(0));

        CompanyCandidate finalChoice = bestNonDistributor != null ? bestNonDistributor : bestOverall;

        logger.info("Enhanced heuristic selected: {} (relevance: {:.2f}, is_distributor: {})",
                finalChoice.slug, finalChoice.relevanceScore, isLikelyDistributor(finalChoice));

        return finalChoice;
    }

    /**
     * Calculates relevance score between company name and candidate
     */
    private double calculateRelevanceScore(String companyName, CompanyCandidate candidate) {
        double score = 0.0;
        String lowerCompany = companyName.toLowerCase();
        String lowerSlug = candidate.slug.toLowerCase();
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();

        // Exact slug match
        if (lowerSlug.equals(lowerCompany)) {
            score += 100.0;
        }

        // Slug contains company name or vice versa
        if (lowerSlug.contains(lowerCompany) || lowerCompany.contains(lowerSlug)) {
            score += 50.0;
        }

        // Title contains company name
        if (lowerTitle.contains(lowerCompany)) {
            score += 30.0;
        }

        // Content contains company name
        if (lowerContent.contains(lowerCompany)) {
            score += 20.0;
        }

        // Penalties for likely distributors or unrelated content
        if (isLikelyDistributor(candidate)) {
            score -= 25.0;
        }

        return Math.max(0.0, score);
    }

    /**
     * Checks if candidate is likely a distributor rather than the main company
     */
    private boolean isLikelyDistributor(CompanyCandidate candidate) {
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();
        String lowerSlug = candidate.slug.toLowerCase();

        String[] distributorIndicators = {
            "distributor", "dealer", "partner", "reseller", "retailer", 
            "authorized", "official dealer", "sales partner"
        };

        for (String indicator : distributorIndicators) {
            if (lowerTitle.contains(indicator) || lowerContent.contains(indicator) || lowerSlug.contains(indicator)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates if a LinkedIn company URL exists using HEAD request
     */
    private boolean validateLinkedInCompanyExists(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        String url = "https://www.linkedin.com/company/" + slug;
        logger.debug("Validating LinkedIn company URL: {}", url);

        try {
            java.net.URI uri = java.net.URI.create(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", userAgents[random.nextInt(userAgents.length)]);

            int responseCode = connection.getResponseCode();
            logger.debug("HEAD request to {} returned status code: {}", url, responseCode);

            return responseCode == 200;

        } catch (Exception e) {
            logger.debug("Failed to validate LinkedIn URL {}: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Finds valid LinkedIn slug by testing variations
     */
    private String findValidLinkedInSlug(String companyName) {
        logger.info("Finding valid LinkedIn slug for company: {}", companyName);

        List<String> slugCandidates = generateSlugVariations(companyName);

        for (String slug : slugCandidates) {
            if (validateLinkedInCompanyExists(slug)) {
                logger.info("✓ Valid LinkedIn slug found: {} -> {}", companyName, slug);
                return slug;
            }
        }

        logger.info("No valid LinkedIn slug found for: {}", companyName);
        return null;
    }

    /**
     * Generates slug variations for testing
     */
    private List<String> generateSlugVariations(String companyName) {
        List<String> slugCandidates = new ArrayList<>();
        String lower = companyName.toLowerCase().trim();

        // Generate basic slug
        String basicSlug = lower.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        slugCandidates.add(basicSlug);

        // Corporate suffixes
        String[] suffixes = {"-inc", "-corp", "-corporation", "-ltd", "-llc", "-technologies", "-labs"};
        for (String suffix : suffixes) {
            slugCandidates.add(basicSlug + suffix);
        }

        // Remove existing suffixes variations
        String[] removable = {"-inc", "-corp", "-corporation", "-ltd", "-llc", "-company", "-co"};
        for (String remove : removable) {
            if (basicSlug.endsWith(remove)) {
                String clean = basicSlug.substring(0, basicSlug.length() - remove.length());
                slugCandidates.add(clean);
            }
        }

        return slugCandidates.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Generates fallback slug when no valid candidates found
     */
    private String generateFallbackSlug(String companyName, List<Map<String, Object>> searchResults) {
        logger.info("Attempting to generate fallback slug for '{}' from {} search results", companyName, searchResults.size());

        // Look for any LinkedIn URLs in search results
        for (Map<String, Object> result : searchResults) {
            String url = (String) result.get("url");
            if (url != null && url.contains("linkedin.com/company/")) {
                String slug = extractLinkedInSlugFromUrl(url);
                if (!slug.isEmpty() && slug.length() > 2) {
                    logger.info("Generated fallback slug from search results: {}", slug);
                    return slug;
                }
            }
        }

        // Final fallback: generate from company name
        return generateEnhancedFallbackSlug(companyName);
    }

    /**
     * Enhanced fallback slug generation with multiple patterns
     */
    private String generateEnhancedFallbackSlug(String companyName) {
        logger.info("Generating enhanced fallback slug for: {}", companyName);

        // Pattern 1: Simple normalization
        String pattern1 = companyName.toLowerCase().replaceAll("[^a-z0-9-]", "");

        // Pattern 2: Replace spaces with hyphens
        String pattern2 = companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Pattern 3: Remove corporate suffixes
        String pattern3 = companyName.toLowerCase()
                .replaceAll("\\b(inc|corp|corporation|ltd|llc|company)\\b", "")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Choose the best pattern (prefer shorter, cleaner ones)
        String[] patterns = {pattern1, pattern2, pattern3};
        String bestPattern = pattern1;

        for (String pattern : patterns) {
            if (!pattern.isEmpty() && pattern.length() > 1 && pattern.length() < bestPattern.length()) {
                bestPattern = pattern;
            }
        }

        logger.info("Enhanced fallback slug selection: {} -> {}", companyName, bestPattern);
        return bestPattern;
    }
}