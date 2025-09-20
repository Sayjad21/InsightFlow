package com.insightflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TavilyUtil {

    private static final Logger logger = LoggerFactory.getLogger(TavilyUtil.class);

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    private final WebClient webClient = WebClient.builder().baseUrl("https://api.tavily.com").build();

    /**
     * Performs a Tavily search, mirroring TavilySearchResults.
     * 
     * @param query      The search query.
     * @param maxResults Max results (default 3 from code).
     * @return List of result maps (each with "url", "content").
     */
    public List<Map<String, Object>> search(String query, int maxResults, List<String> includeDomains) {
        logger.debug("Executing Tavily search: '{}' (max results: {})", query, maxResults);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("api_key", tavilyApiKey);
        requestBody.put("query", query);
        requestBody.put("search_depth", "basic");
        requestBody.put("include_answer", false);
        requestBody.put("include_images", false);
        requestBody.put("max_results", maxResults);

        if (includeDomains != null && !includeDomains.isEmpty()) {
            requestBody.put("include_domains", includeDomains);
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = webClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> (List<Map<String, Object>>) response.get("results"))
                    .block(); // Synchronous for simplicity; use async in production

            logger.debug("Tavily search completed: {} results returned", results != null ? results.size() : 0);
            return results != null ? results : new ArrayList<>();

        } catch (Exception e) {
            logger.error("Tavily search failed for query '{}': {}", query, e.getMessage());
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Enhanced LinkedIn company search with multiple search strategies
     * 
     * @param companyName The company name to search for
     * @return List of LinkedIn company search results
     */
    public List<Map<String, Object>> searchLinkedInCompany(String companyName) {
        logger.info("Starting enhanced LinkedIn company search for: {}", companyName);

        List<Map<String, Object>> allResults = new ArrayList<>();

        // Strategy 1: Force LinkedIn company domain search (most targeted)
        String forcedLinkedInQuery = "site:linkedin.com/company " + companyName;
        List<Map<String, Object>> forcedResults = search(companyName, 5, List.of("linkedin.com/company"));
        logger.info("Forced LinkedIn domain search returned {} results", forcedResults.size());
        allResults.addAll(forcedResults);

        // Strategy 2: Try with exact path structure
        if (allResults.size() < 2) {
            String exactPathQuery = "site:linkedin.com/company/ \"" + companyName + "\"";
            List<Map<String, Object>> exactResults = search(exactPathQuery, 3, List.of("linkedin.com/company"));
            logger.info("Exact LinkedIn path search returned {} results", exactResults.size());
            allResults.addAll(exactResults);
        }

        // Strategy 3: Try common company variations with forced domain
        if (allResults.size() < 2) {
            List<String> variations = generateLinkedInCompanyVariations(companyName);
            for (String variation : variations) {
                String variationQuery = "site:linkedin.com/company " + variation;
                List<Map<String, Object>> variationResults = search(variationQuery, 2, List.of("linkedin.com/company"));
                logger.info("LinkedIn variation '{}' search returned {} results", variation, variationResults.size());
                allResults.addAll(variationResults);
                if (allResults.size() >= 5)
                    break; // Limit total results
            }
        }

        // Strategy 4: Broader LinkedIn search (only if still insufficient)
        if (allResults.size() < 3) {
            String broadQuery = "linkedin.com \"" + companyName + "\" company profile";
            List<Map<String, Object>> broadResults = search(broadQuery, 3, List.of("linkedin.com/company"));
            logger.info("Broad LinkedIn search returned {} results", broadResults.size());
            allResults.addAll(broadResults);
        }

        // Deduplicate results based on URL
        List<Map<String, Object>> uniqueResults = new ArrayList<>();
        List<String> seenUrls = new ArrayList<>();

        for (Map<String, Object> result : allResults) {
            String url = (String) result.get("url");
            if (url != null && !seenUrls.contains(url)) {
                seenUrls.add(url);
                uniqueResults.add(result);
            }
        }

        logger.info("Enhanced LinkedIn search completed: {} unique results for '{}'", uniqueResults.size(),
                companyName);
        return uniqueResults;
    }

    /**
     * Generates LinkedIn-specific company name variations optimized for company
     * page discovery
     */
    private List<String> generateLinkedInCompanyVariations(String companyName) {
        List<String> variations = new ArrayList<>();
        String lower = companyName.toLowerCase().trim();

        // Always include original
        variations.add(companyName);

        // AI/Tech company variations (very common on LinkedIn)
        if (!lower.contains("-ai") && (lower.contains("ai") || lower.contains("artificial"))) {
            variations.add(companyName + "-ai");
            if (!lower.endsWith("ai")) {
                variations.add(companyName.replaceFirst("(?i)\\bai\\b", "").trim() + "-ai");
            }
        }

        // Corporate suffix variations
        String[] suffixes = { "-inc", "-corp", "-corporation", "-ltd", "-llc", "-technologies", "-labs", "-solutions" };
        for (String suffix : suffixes) {
            if (!lower.endsWith(suffix.substring(1))) { // Remove the dash for comparison
                variations.add(companyName + suffix);
            }
        }

        // Remove existing suffixes and try clean name
        String[] removableSuffixes = { "inc", "corp", "corporation", "ltd", "llc", "company", "co", "technologies",
                "tech" };
        for (String suffix : removableSuffixes) {
            if (lower.endsWith(" " + suffix)) {
                String cleanName = companyName.substring(0, companyName.length() - suffix.length() - 1);
                if (!cleanName.trim().isEmpty()) {
                    variations.add(cleanName.trim());
                }
            }
        }

        // Space vs hyphen variations
        if (companyName.contains(" ")) {
            variations.add(companyName.replace(" ", "-"));
        }
        if (companyName.contains("-")) {
            variations.add(companyName.replace("-", " "));
        }

        logger.debug("Generated {} LinkedIn variations for '{}': {}", variations.size(), companyName, variations);
        return variations.stream().distinct().collect(java.util.stream.Collectors.toList());
    }

    /**
     * Performs a deep search with advanced strategies for hard-to-find companies
     * 
     * @param companyName       The company name
     * @param includeVariations Whether to include company name variations
     * @return List of search results
     */
    public List<Map<String, Object>> deepSearchLinkedInCompany(String companyName, boolean includeVariations) {
        logger.info("Starting deep LinkedIn company search for: {} (variations: {})", companyName, includeVariations);

        List<Map<String, Object>> allResults = new ArrayList<>();

        // First try the standard search
        allResults.addAll(searchLinkedInCompany(companyName));

        if (includeVariations && allResults.size() < 2) {
            logger.info("Standard search insufficient, trying company name variations...");

            // Generate company name variations
            List<String> variations = generateCompanyNameVariations(companyName);

            for (String variation : variations) {
                if (!variation.equals(companyName)) {
                    logger.debug("Searching for variation: {}", variation);
                    String variationQuery = "site:linkedin.com/company " + variation;
                    List<Map<String, Object>> variationResults = search(variationQuery, 2,
                            List.of("linkedin.com/company"));
                    allResults.addAll(variationResults);
                }
            }
        }

        // Deduplicate
        List<Map<String, Object>> uniqueResults = new ArrayList<>();
        List<String> seenUrls = new ArrayList<>();

        for (Map<String, Object> result : allResults) {
            String url = (String) result.get("url");
            if (url != null && !seenUrls.contains(url)) {
                seenUrls.add(url);
                uniqueResults.add(result);
            }
        }

        logger.info("Deep search completed: {} unique results for '{}'", uniqueResults.size(), companyName);
        return uniqueResults;
    }

    /**
     * Generates common variations of a company name for searching
     */
    private List<String> generateCompanyNameVariations(String companyName) {
        List<String> variations = new ArrayList<>();
        String lower = companyName.toLowerCase();

        variations.add(companyName); // Original

        // Remove common suffixes
        String[] suffixes = { "inc", "corp", "corporation", "ltd", "llc", "company", "co" };
        for (String suffix : suffixes) {
            if (lower.endsWith(" " + suffix)) {
                String withoutSuffix = companyName.substring(0, companyName.length() - suffix.length() - 1);
                variations.add(withoutSuffix);
            }
        }

        // Add common tech company variations
        if (!lower.contains("technologies")
                && (lower.contains("tech") || lower.contains("ai") || lower.contains("software"))) {
            variations.add(companyName + " Technologies");
        }

        // Add "The" prefix if not present
        if (!lower.startsWith("the ")) {
            variations.add("The " + companyName);
        }

        // Remove "The" prefix if present
        if (lower.startsWith("the ")) {
            variations.add(companyName.substring(4));
        }

        logger.debug("Generated {} variations for company '{}'", variations.size(), companyName);
        return variations;
    }

    /**
     * Validates if a LinkedIn company URL exists using HEAD request
     * 
     * @param slug The LinkedIn company slug to test
     * @return true if the URL returns 200, false otherwise
     */
    public boolean validateLinkedInCompanyExists(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        String url = "https://www.linkedin.com/company/" + slug;
        logger.debug("Validating LinkedIn company URL: {}", url);

        try {
            URI uri = URI.create(url);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5 second timeout
            connection.setReadTimeout(5000);

            // Set user agent to avoid blocking
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:118.0) Gecko/20100101 Firefox/118.0");

            int responseCode = connection.getResponseCode();
            logger.debug("HEAD request to {} returned status code: {}", url, responseCode);

            return responseCode == 200;

        } catch (IOException e) {
            logger.debug("Failed to validate LinkedIn URL {}: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Generates and tests LinkedIn slug variations for a company
     * 
     * @param companyName The company name
     * @return The first valid LinkedIn slug found, or null if none exist
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

        // AI company variations
        if (lower.contains("ai") || lower.contains("artificial")) {
            slugCandidates.add(basicSlug + "-ai");
            slugCandidates.add(basicSlug.replace("ai", "") + "-ai");
        }

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
}