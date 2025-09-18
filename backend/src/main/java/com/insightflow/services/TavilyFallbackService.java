package com.insightflow.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.utils.AiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TavilyFallbackService {

    private static final Logger logger = LoggerFactory.getLogger(TavilyFallbackService.class);

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    @Autowired
    private AiUtil aiUtil;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TavilyFallbackService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fallback LinkedIn analysis using Tavily crawl API
     */
    public String getLinkedInAnalysisFallback(String companyName) {
        logger.info("====== STARTING TAVILY FALLBACK ANALYSIS FOR: '{}' ======", companyName);
        long analysisStartTime = System.currentTimeMillis();

        try {
            // Step 1: Crawl LinkedIn company page using Tavily
            String linkedinUrl = "https://www.linkedin.com/company/" + generateLinkedInSlug(companyName) + "/";
            logger.info("Crawling LinkedIn URL: {}", linkedinUrl);

            String crawlResponse = crawlLinkedInPage(linkedinUrl);
            if (crawlResponse == null || crawlResponse.isEmpty()) {
                logger.warn("No content retrieved from Tavily crawl, generating minimal analysis");
                return generateMinimalAnalysis(companyName);
            }

            // Step 2: Extract and process content
            String processedContent = extractAndProcessContent(crawlResponse, companyName);
            if (processedContent.length() < 100) {
                logger.warn("Insufficient content extracted, generating minimal analysis");
                return generateMinimalAnalysis(companyName);
            }

            // Step 3: Generate AI analysis
            String analysis = generateAIAnalysis(companyName, processedContent);

            long analysisEndTime = System.currentTimeMillis();
            long totalDuration = analysisEndTime - analysisStartTime;

            logger.info("====== TAVILY FALLBACK ANALYSIS COMPLETED ======");
            logger.info("Company: {}", companyName);
            logger.info("Total Duration: {} ms ({} seconds)", totalDuration, totalDuration / 1000.0);
            logger.info("Content Length: {} characters", analysis.length());

            return "<strong>LinkedIn Analysis of " + companyName + "</strong><br><br>" + analysis;

        } catch (Exception e) {
            logger.error("Tavily fallback analysis failed for {}: {}", companyName, e.getMessage(), e);
            return generateMinimalAnalysis(companyName);
        }
    }

    /**
     * Crawl LinkedIn company page using Tavily API
     */
    private String crawlLinkedInPage(String linkedinUrl) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("url", linkedinUrl);
            requestBody.put("instructions", 
                "Extract meaningful data about the company. Focus on company description, about section, recent posts, " +
                "company size, industry information, and key business activities. " +
                "Ignore LinkedIn login/signup elements, navigation menus, and unrelated content. " +
                "Prioritize plain text content over images or external URLs.");
            requestBody.put("extract_depth", "advanced");
            requestBody.put("categories", Arrays.asList("About", "Careers", "Enterprise", "Documentation"));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tavily.com/crawl"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + tavilyApiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Tavily crawl API response status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                logger.error("Tavily crawl API error: {} - {}", response.statusCode(), response.body());
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to crawl LinkedIn page via Tavily: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract and process content from Tavily response
     */
    private String extractAndProcessContent(String crawlResponse, String companyName) {
        try {
            JsonNode responseNode = objectMapper.readTree(crawlResponse);
            StringBuilder processedContent = new StringBuilder();

            // Extract base URL info
            String baseUrl = responseNode.path("base_url").asText();
            if (!baseUrl.isEmpty()) {
                processedContent.append("Source: ").append(baseUrl).append("\n\n");
            }

            // Process results array
            JsonNode resultsNode = responseNode.path("results");
            if (resultsNode.isArray()) {
                for (JsonNode result : resultsNode) {
                    String url = result.path("url").asText();
                    String rawContent = result.path("raw_content").asText();

                    if (!rawContent.isEmpty()) {
                        // Clean and process the raw content
                        String cleanContent = cleanTavilyContent(rawContent, companyName);
                        if (!cleanContent.isEmpty()) {
                            processedContent.append("=== Content from: ").append(url).append(" ===\n");
                            processedContent.append(cleanContent).append("\n\n");
                        }
                    }
                }
            }

            String finalContent = processedContent.toString().trim();
            
            // Truncate if too long (similar to original scraping service)
            if (finalContent.length() > 25000) {
                finalContent = finalContent.substring(0, 25000) + "... [Truncated for analysis]";
                logger.info("Truncated Tavily content to {} chars to avoid LLM timeouts", finalContent.length());
            }

            logger.info("Processed Tavily content length: {} characters", finalContent.length());
            return finalContent;

        } catch (Exception e) {
            logger.error("Failed to extract content from Tavily response: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Clean Tavily raw content similar to LinkedIn scraping service
     */
    private String cleanTavilyContent(String rawContent, String companyName) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }

        StringBuilder cleanedContent = new StringBuilder();
        String[] lines = rawContent.split("\n");
        
        // Patterns to filter out irrelevant content
        Pattern skipPatterns = Pattern.compile(
            "(?i)(log in|sign up|skip to|back to main|menu|navigation|cookie|manage cookies|" +
            "opens in a new window|subscribe|follow us|download|contact sales|help center|" +
            "terms of use|privacy policy|home|search|filter|sort by)"
        );

        Pattern relevantPatterns = Pattern.compile(
            "(?i)(about|company|business|mission|vision|values|products|services|team|" +
            "employees|founded|headquarters|industry|revenue|customers|clients|innovation|" +
            "technology|leadership|strategy|growth|market|solutions)"
        );

        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip empty lines
            if (trimmedLine.isEmpty()) continue;
            
            // Skip navigation and irrelevant elements
            if (skipPatterns.matcher(trimmedLine).find()) continue;
            
            // Skip very short lines (likely navigation elements)
            if (trimmedLine.length() < 10) continue;
            
            // Skip lines that are just symbols or numbers
            if (trimmedLine.matches("^[\\d\\s\\-\\|\\*\\+\\.]+$")) continue;
            
            // Prioritize relevant content
            if (relevantPatterns.matcher(trimmedLine).find()) {
                cleanedContent.append(trimmedLine).append("\n");
            } else if (trimmedLine.length() > 30 && containsBusinessContent(trimmedLine)) {
                // Include longer lines that seem to contain business content
                cleanedContent.append(trimmedLine).append("\n");
            }
        }

        String result = cleanedContent.toString().trim();
        
        // Remove duplicate lines
        Set<String> uniqueLines = new LinkedHashSet<>();
        for (String line : result.split("\n")) {
            if (!line.trim().isEmpty()) {
                uniqueLines.add(line.trim());
            }
        }
        
        return String.join("\n", uniqueLines);
    }

    /**
     * Check if line contains business-relevant content
     */
    private boolean containsBusinessContent(String line) {
        String lowerLine = line.toLowerCase();
        
        // Check for business-related keywords
        String[] businessKeywords = {
            "company", "business", "enterprise", "solution", "service", "product", 
            "customer", "client", "market", "industry", "technology", "innovation",
            "team", "employee", "revenue", "growth", "strategy", "mission", "vision"
        };
        
        for (String keyword : businessKeywords) {
            if (lowerLine.contains(keyword)) {
                return true;
            }
        }
        
        // Check if it's a substantial sentence (contains verbs and nouns)
        if (line.split("\\s+").length > 8 && line.contains(" ")) {
            return true;
        }
        
        return false;
    }

    /**
     * Generate AI analysis from processed content
     */
    private String generateAIAnalysis(String companyName, String processedContent) {
        try {
            String template = aiUtil.getLinkedInAnalysisTemplate();
            
            // Prepare content similar to original scraping service
            String optimizedContent = prepareContentForAnalysis(companyName, processedContent);
            
            Map<String, Object> variables = Map.of(
                "content", optimizedContent,
                "company_name", companyName,
                "source", "Tavily Crawl API"
            );
            
            String analysis = aiUtil.invokeWithTemplate(template, variables);
            
            // Format for HTML similar to original service
            if (analysis != null && !analysis.trim().isEmpty() && analysis.length() > 200) {
                return formatAnalysisForHtml(analysis);
            } else {
                logger.warn("AI analysis insufficient, using fallback");
                return generateSimpleFallback(companyName, processedContent);
            }
            
        } catch (Exception e) {
            logger.error("AI analysis generation failed: {}", e.getMessage());
            return generateSimpleFallback(companyName, processedContent);
        }
    }

    /**
     * Prepare content for AI analysis
     */
    private String prepareContentForAnalysis(String companyName, String rawContent) {
        StringBuilder content = new StringBuilder();
        
        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");
        content.append("Source: LinkedIn (via Tavily crawl API)\n\n");
        
        content.append("=== EXTRACTED CONTENT ===\n");
        content.append(rawContent).append("\n\n");
        
        content.append("=== ANALYSIS REQUEST ===\n");
        content.append("Please analyze this company's LinkedIn presence and provide strategic insights.\n");
        
        return content.toString();
    }

    /**
     * Format analysis for HTML output
     */
    private String formatAnalysisForHtml(String analysis) {
        return analysis.replace("\n", "<br>")
                .replaceAll("<br>{2,}", "<br>")
                .replaceAll("####\\s*([^<]+)", "<strong>$1</strong><br>")
                .replaceAll("-\\s*", "- ")
                .replaceAll("\\*\\*([^\\*]+)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("([a-zA-Z])<br>([a-zA-Z])", "$1 $2")
                .replaceAll("<br>\\s*-", "<br>-");
    }

    /**
     * Generate simple fallback analysis
     */
    private String generateSimpleFallback(String companyName, String content) {
        StringBuilder fallback = new StringBuilder();
        
        fallback.append("#### Strategic Analysis: ").append(companyName).append(" (Fallback)<br><br>");
        
        fallback.append("#### I. Company Overview<br><br>");
        fallback.append("**Company:** ").append(companyName).append("<br>");
        fallback.append("**Analysis Method:** Tavily crawl API fallback<br>");
        
        if (content.length() > 100) {
            String shortContent = content.length() > 300 ? content.substring(0, 300) + "..." : content;
            fallback.append("**Available Information:** ").append(shortContent.replaceAll("\n", " ")).append("<br><br>");
        }
        
        fallback.append("#### II. Analysis Limitations<br><br>");
        fallback.append("**Data Source:** Public web content only<br>");
        fallback.append("**Scope:** Limited to publicly accessible information<br>");
        fallback.append("**Recommendation:** For comprehensive analysis, consider additional data sources<br>");
        
        return fallback.toString();
    }

    /**
     * Generate minimal analysis when everything fails
     */
    private String generateMinimalAnalysis(String companyName) {
        return "<strong>Company Analysis: " + companyName + "</strong><br><br>" +
               "<em>Analysis could not be completed due to data access limitations.</em><br><br>" +
               "<strong>Company:</strong> " + companyName + "<br>" +
               "<strong>Status:</strong> Unable to retrieve sufficient data<br>" +
               "<strong>Recommendation:</strong> Try again later or use alternative data sources";
    }

    /**
     * Generate LinkedIn company slug from company name
     */
    private String generateLinkedInSlug(String companyName) {
        return companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}