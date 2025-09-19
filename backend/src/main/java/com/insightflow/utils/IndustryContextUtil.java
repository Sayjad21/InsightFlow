package com.insightflow.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Centralized utility for industry context analysis and competitor
 * identification.
 * Provides both Tavily API-powered and heuristic-based industry analysis.
 */
@Component
public class IndustryContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(IndustryContextUtil.class);

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Industry definitions with competitors
    private static final Map<String, IndustryDefinition> INDUSTRY_DEFINITIONS = new HashMap<>();

    static {
        INDUSTRY_DEFINITIONS.put("AI/ML", new IndustryDefinition(
                Arrays.asList("artificial intelligence", " ai ", "machine learning", "gpt", "llm", "neural",
                        "deep learning"),
                "AI/ML Technology",
                Arrays.asList("OpenAI", "Google", "Microsoft", "Anthropic", "Meta", "NVIDIA")));

        INDUSTRY_DEFINITIONS.put("Cloud Computing", new IndustryDefinition(
                Arrays.asList("cloud", "azure", "aws", "infrastructure", "saas", "paas", "iaas"),
                "Cloud Computing",
                Arrays.asList("AWS", "Microsoft Azure", "Google Cloud", "Oracle Cloud", "IBM Cloud")));

        INDUSTRY_DEFINITIONS.put("Social Media", new IndustryDefinition(
                Arrays.asList("social", "platform", "network", "social media", "community"),
                "Social Media/Platforms",
                Arrays.asList("Meta", "Twitter/X", "TikTok", "LinkedIn", "YouTube", "Snapchat")));

        INDUSTRY_DEFINITIONS.put("Digital Advertising", new IndustryDefinition(
                Arrays.asList("search", "advertising", "marketing", "adtech", "programmatic", "ads"),
                "Digital Advertising",
                Arrays.asList("Google", "Meta", "Amazon", "Microsoft", "The Trade Desk")));

        INDUSTRY_DEFINITIONS.put("Electric Vehicles", new IndustryDefinition(
                Arrays.asList("electric", "automotive", "vehicle", "ev", "battery", "autonomous"),
                "Electric Vehicles",
                Arrays.asList("Tesla", "BYD", "Toyota", "Volkswagen", "GM", "Ford")));

        INDUSTRY_DEFINITIONS.put("Digital Media", new IndustryDefinition(
                Arrays.asList("entertainment", "streaming", "media", "content", "video", "music"),
                "Digital Media",
                Arrays.asList("Netflix", "Disney", "Amazon Prime", "Apple", "Spotify", "YouTube")));

        INDUSTRY_DEFINITIONS.put("E-commerce", new IndustryDefinition(
                Arrays.asList("ecommerce", "e-commerce", "retail", "marketplace", "shopping", "commerce"),
                "E-commerce",
                Arrays.asList("Amazon", "Shopify", "Alibaba", "eBay", "Walmart", "Target")));

        INDUSTRY_DEFINITIONS.put("Fintech", new IndustryDefinition(
                Arrays.asList("fintech", "financial", "banking", "payments", "cryptocurrency", "blockchain"),
                "Financial Technology",
                Arrays.asList("PayPal", "Square", "Stripe", "Coinbase", "Robinhood", "Klarna")));
    }

    public IndustryContextUtil() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Data class to hold industry definitions
     */
    private static class IndustryDefinition {
        public final List<String> keywords;
        public final String displayName;
        public final List<String> competitors;

        public IndustryDefinition(List<String> keywords, String displayName, List<String> competitors) {
            this.keywords = keywords;
            this.displayName = displayName;
            this.competitors = competitors;
        }
    }

    /**
     * Result class for industry analysis
     */
    public static class IndustryAnalysisResult {
        public final String industryContext;
        public final String competitorAnalysis;
        public final List<String> identifiedIndustries;
        public final String dataSource;

        public IndustryAnalysisResult(String industryContext, String competitorAnalysis,
                List<String> identifiedIndustries, String dataSource) {
            this.industryContext = industryContext;
            this.competitorAnalysis = competitorAnalysis;
            this.identifiedIndustries = identifiedIndustries;
            this.dataSource = dataSource;
        }
    }

    /**
     * Main method to analyze industry context with Tavily API integration
     * 
     * @param companyName The company name
     * @param description Company description
     * @param posts       List of company posts
     * @return Complete industry analysis result
     */
    public IndustryAnalysisResult analyzeIndustryContext(String companyName, String description, List<String> posts) {
        logger.debug("Starting industry context analysis for: {}", companyName);

        // Try Tavily API first for dynamic competitor analysis
        String tavilyCompetitors = getCompetitorsFromTavily(companyName);
        if (tavilyCompetitors != null && !tavilyCompetitors.trim().isEmpty()) {
            logger.info("✅ Got competitors from Tavily API for: {}", companyName);

            // Also get heuristic analysis for industry classification
            IndustryAnalysisResult heuristicResult = analyzeIndustryContextHeuristic(companyName, description, posts);

            return new IndustryAnalysisResult(
                    tavilyCompetitors,
                    tavilyCompetitors,
                    heuristicResult.identifiedIndustries,
                    "Tavily API");
        }

        // Fallback to heuristic analysis
        logger.debug("Tavily API unavailable, using heuristic analysis");
        return analyzeIndustryContextHeuristic(companyName, description, posts);
    }

    /**
     * Heuristic-based industry analysis (fallback method)
     */
    private IndustryAnalysisResult analyzeIndustryContextHeuristic(String companyName, String description,
            List<String> posts) {
        String combined = (companyName + " " + description + " " + String.join(" ", posts)).toLowerCase();

        List<String> identifiedIndustries = new ArrayList<>();
        List<String> allCompetitors = new ArrayList<>();

        // Analyze against all industry definitions
        for (Map.Entry<String, IndustryDefinition> entry : INDUSTRY_DEFINITIONS.entrySet()) {
            IndustryDefinition industryDef = entry.getValue();
            boolean matchesIndustry = false;

            for (String keyword : industryDef.keywords) {
                if (combined.contains(keyword.toLowerCase())) {
                    matchesIndustry = true;
                    break;
                }
            }

            if (matchesIndustry) {
                identifiedIndustries.add(industryDef.displayName);
                allCompetitors.addAll(industryDef.competitors);
            }
        }

        // Build industry context
        String industryContext;
        String competitorAnalysis;

        if (identifiedIndustries.isEmpty()) {
            industryContext = "Technology sector with various digital services";
            competitorAnalysis = industryContext;
        } else {
            String primaryIndustry = identifiedIndustries.get(0);

            // Get competitors for primary industry
            IndustryDefinition primaryDef = INDUSTRY_DEFINITIONS.values()
                    .stream()
                    .filter(def -> def.displayName.equals(primaryIndustry))
                    .findFirst()
                    .orElse(null);

            if (primaryDef != null) {
                String competitorList = String.join(", ", primaryDef.competitors);
                industryContext = primaryIndustry + ", competing with " + competitorList;
                competitorAnalysis = competitorList;
            } else {
                industryContext = primaryIndustry;
                competitorAnalysis = "General market competition";
            }
        }

        return new IndustryAnalysisResult(
                industryContext,
                competitorAnalysis,
                identifiedIndustries,
                "Heuristic Analysis");
    }

    /**
     * Get competitors using Tavily search API
     */
    private String getCompetitorsFromTavily(String companyName) {
        if (tavilyApiKey == null || tavilyApiKey.trim().isEmpty()) {
            logger.debug("Tavily API key not configured, skipping API call");
            return null;
        }

        try {
            String query = "who are " + companyName + "'s competitors";

            // Build Tavily search request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", tavilyApiKey);
            requestBody.put("query", query);
            requestBody.put("include_answer", "basic");
            requestBody.put("max_results", 5);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tavily.com/search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                JsonNode answerNode = responseJson.get("answer");

                if (answerNode != null && !answerNode.isNull()) {
                    String answer = answerNode.asText();
                    logger.info("✅ Got competitors from Tavily for '{}': {}", companyName,
                            answer.length() > 100 ? answer.substring(0, 100) + "..." : answer);
                    return answer;
                }
            } else {
                logger.debug("Tavily search API returned status code: {} for company: {}",
                        response.statusCode(), companyName);
            }
        } catch (Exception e) {
            logger.debug("Error calling Tavily search API for company '{}': {}", companyName, e.getMessage());
        }

        return null;
    }

    /**
     * Simple method for backward compatibility - returns just the industry context
     * string
     */
    public String getIndustryContext(String companyName, String description, List<String> posts) {
        return analyzeIndustryContext(companyName, description, posts).industryContext;
    }

    /**
     * Get competitor analysis specifically
     */
    public String getCompetitorAnalysis(String companyName, String description, List<String> posts) {
        return analyzeIndustryContext(companyName, description, posts).competitorAnalysis;
    }

    /**
     * Get list of identified industries
     */
    public List<String> getIdentifiedIndustries(String companyName, String description, List<String> posts) {
        return analyzeIndustryContext(companyName, description, posts).identifiedIndustries;
    }
}