package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SentimentFetcherService {

    private final RestTemplate restTemplate;
    private final AiUtil aiUtil;

    // API keys
    @Value("${newsapi.key}")
    private String newsApiKey;
    
    @Value("${google-search.key}")
    private String googleSearchKey;
    
    @Value("${google-search.cx}")
    private String googleCx;

    // Pattern to extract numbers from AI response
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");

    public SentimentFetcherService(AiUtil aiUtil, RestTemplate restTemplate) {
        this.aiUtil = aiUtil;
        this.restTemplate = restTemplate; // Inject instead of creating new
    }

    public List<Map<String, Object>> fetchAndScoreSentiment(String companyName, List<String> sources) {
        List<Map<String, Object>> scoredData = new ArrayList<>();

        for (String source : sources) {
            try {
                Map<String, Object> fetchResult = fetchTextAndUrl(companyName, source);
                String text = (String) fetchResult.get("text");
                String sourceUrl = (String) fetchResult.get("url");

                if (text != null && !text.trim().isEmpty()) {
                    // Get AI analysis
                    String analysis = aiUtil.invokeWithTemplate(
                        aiUtil.getCombinedAnalysisTemplate(), 
                        Map.of("company_name", companyName, "information", text)
                    );
                    
                    // Parse the analysis
                    Map<String, Object> analysisResult = parseAnalysisResult(analysis);
                    
                    // Create data point
                    Map<String, Object> dataPoint = createDataPoint(
                        companyName, source, sourceUrl, text, analysisResult
                    );
                    
                    scoredData.add(dataPoint);
                }
            } catch (Exception e) {
                // Log error but continue with other sources
                System.err.println("Error processing source " + source + ": " + e.getMessage());
            }
        }
        return scoredData;
    }

    private Map<String, Object> fetchTextAndUrl(String companyName, String source) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if ("news".equals(source)) {
                String url = UriComponentsBuilder.fromHttpUrl("https://newsapi.org/v2/everything")
                    .queryParam("q", companyName)
                    .queryParam("apiKey", newsApiKey)
                    .queryParam("sortBy", "publishedAt")
                    .queryParam("pageSize", 5)
                    .build().toUriString();

                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> articles = (List<Map<String, Object>>) 
                        response.getBody().get("articles");
                    
                    if (articles != null && !articles.isEmpty()) {
                        // Get text from first article
                        Map<String, Object> firstArticle = articles.get(0);
                        String text = (String) firstArticle.get("title") + " " + 
                                     firstArticle.get("description");
                        
                        result.put("text", text);
                        result.put("url", firstArticle.get("url")); // REAL URL
                        
                        // 👇 Add print here
                        System.out.println("[FETCHED-NEWS] " + text + " (URL: " + firstArticle.get("url") + ")");
                    }
                }
            } 
            else if ("social".equals(source)) {
                String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/customsearch/v1")
                    .queryParam("key", googleSearchKey)
                    .queryParam("cx", googleCx)
                    .queryParam("q", companyName + " social sentiment")
                    .queryParam("num", 5)
                    .build().toUriString();

                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) 
                        response.getBody().get("items");
                    
                    if (items != null && !items.isEmpty()) {
                        // Get text from first item
                        Map<String, Object> firstItem = items.get(0);
                        String text = (String) firstItem.get("snippet");
                        
                        result.put("text", text);
                        result.put("url", firstItem.get("link")); // REAL URL
                        
                        // 👇 Add print here
                        System.out.println("[FETCHED-SOCIAL] " + text + " (URL: " + firstItem.get("link") + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching from " + source + ": " + e.getMessage());
        }
        
        return result;
    }

    private Map<String, Object> parseAnalysisResult(String analysis) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First try to parse as JSON
            Map<String, Object> jsonResult = aiUtil.parseJsonToMap(analysis);
            if (jsonResult.containsKey("sentiment_score") && jsonResult.containsKey("risk_rating")) {
                Double sentiment = (Double) jsonResult.get("sentiment_score");
                Double risk = (Double) jsonResult.get("risk_rating");
                
                // Validate parsed values
                if (sentiment != null && sentiment > 0 && risk != null && risk >= 0) {
                    return jsonResult;
                }
            }
        } catch (Exception e) {
            System.out.println("JSON parsing failed for analysis: " + analysis);
        }
        
        // If JSON parsing fails or values are invalid, try to extract numbers
        Matcher matcher = NUMBER_PATTERN.matcher(analysis);
        List<Double> numbers = new ArrayList<>();
        
        while (matcher.find()) {
            double num = Double.parseDouble(matcher.group());
            if (num > 0) { // Only add positive numbers
                numbers.add(num);
            }
        }
        
        if (numbers.size() >= 2) {
            result.put("sentiment_score", numbers.get(0));
            result.put("risk_rating", numbers.get(1));
        } else {
            // If we can't get valid analysis, return null to skip this item
            System.out.println("Could not extract valid sentiment scores from: " + analysis);
            result.put("sentiment_score", 0.0); // This will be caught by validation
            result.put("risk_rating", 0.0);
        }
        
        return result;
    }

    private Map<String, Object> createDataPoint(String companyName, String sourceType, 
                                              String sourceUrl, String text, 
                                              Map<String, Object> analysisResult) {
        // Validate analysis results before creating data point
        Double sentimentScore = (Double) analysisResult.get("sentiment_score");
        Double riskRating = (Double) analysisResult.get("risk_rating");
        
        // Skip if both are 0 or either is invalid
        if ((sentimentScore == null || sentimentScore <= 0) && 
            (riskRating == null || riskRating <= 0)) {
            System.out.println("Skipping invalid analysis for " + companyName + 
                             " - sentiment: " + sentimentScore + ", risk: " + riskRating);
            return null; // Return null to skip this data point
        }
        
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("companyName", companyName);
        dataPoint.put("sentimentScore", sentimentScore);
        dataPoint.put("riskRating", riskRating);
        dataPoint.put("sourceType", sourceType);
        dataPoint.put("sourceIdentifier", sourceUrl != null ? sourceUrl : "unknown");
        dataPoint.put("timestamp", LocalDateTime.now());
        
        // Real metadata based on actual content
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("text_length", text.length());
        metadata.put("word_count", text.split("\\s+").length);
        metadata.put("source_reachable", sourceUrl != null);
        metadata.put("processing_timestamp", LocalDateTime.now().toString());
        
        dataPoint.put("metadata", metadata);
        return dataPoint;
    }
}