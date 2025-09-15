package com.insightflow;

import com.insightflow.utils.AiUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.insightflow.services.SentimentFetcherService;
public class SentimentFetcherServiceTest {

    private RestTemplate restTemplate;
    private AiUtil aiUtil;
    private SentimentFetcherService service;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        aiUtil = Mockito.mock(AiUtil.class);
        service = new SentimentFetcherService(aiUtil, restTemplate);      
    }

    @Test
    void testFetchAndScoreSentiment_newsSource() {
        // Mock NewsAPI response
        Map<String, Object> article = new HashMap<>();
        article.put("title", "Tesla stock rises");
        article.put("description", "Investors are optimistic about Tesla’s growth.");
        article.put("url", "http://example.com/tesla");

        Map<String, Object> body = new HashMap<>();
        body.put("articles", List.of(article));

        when(restTemplate.getForEntity(contains("newsapi.org"), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        // Mock AI analysis
        when(aiUtil.getCombinedAnalysisTemplate()).thenReturn("dummy template");
        when(aiUtil.invokeWithTemplate(anyString(), anyMap()))
                .thenReturn("{\"sentiment_score\": 80.5, \"risk_rating\": 3.2}");
        when(aiUtil.parseJsonToMap(anyString()))
                .thenReturn(Map.of("sentiment_score", 80.5, "risk_rating", 3.2));

        // Call service
        var results = service.fetchAndScoreSentiment("Tesla", List.of("news"));

        // Print results
        System.out.println("News source test results:");
        results.forEach(System.out::println);
    }

    @Test
    void testFetchAndScoreSentiment_socialSource() {
        // Mock Google Custom Search response
        Map<String, Object> item = new HashMap<>();
        item.put("snippet", "People on Twitter are discussing Tesla negatively.");
        item.put("link", "http://twitter.com/tesla123");

        Map<String, Object> body = new HashMap<>();
        body.put("items", List.of(item));

        when(restTemplate.getForEntity(contains("googleapis.com"), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        // Mock AI analysis returns non-JSON
        when(aiUtil.getCombinedAnalysisTemplate()).thenReturn("dummy template");
        when(aiUtil.invokeWithTemplate(anyString(), anyMap()))
                .thenReturn("Sentiment: 45.2, Risk: 6.8");
        when(aiUtil.parseJsonToMap(anyString()))
                .thenThrow(new RuntimeException("Not JSON"));

        // Call service
        var results = service.fetchAndScoreSentiment("Tesla", List.of("social"));

        // Print results
        System.out.println("Social source test results:");
        results.forEach(System.out::println);
    }

    @Test
    void testFetchAndScoreSentiment_withEmptyResponse() {
        // Mock empty NewsAPI response
        Map<String, Object> body = new HashMap<>();
        body.put("articles", Collections.emptyList());

        when(restTemplate.getForEntity(contains("newsapi.org"), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        // Call service
        var results = service.fetchAndScoreSentiment("Tesla", List.of("news"));

        // Print results
        System.out.println("Empty response test results:");
        System.out.println(results); // Should be []
    }
}
