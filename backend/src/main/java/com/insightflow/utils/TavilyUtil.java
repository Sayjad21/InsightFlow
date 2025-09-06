package com.insightflow.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class TavilyUtil {

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    private final WebClient webClient = WebClient.builder().baseUrl("https://api.tavily.com").build();

    /**
     * Performs a Tavily search, mirroring TavilySearchResults.
     * @param query The search query.
     * @param maxResults Max results (default 3 from code).
     * @return List of result maps (each with "url", "content").
     */
    public List<Map<String, Object>> search(String query, int maxResults) {
        Map<String, Object> requestBody = Map.of(
                "api_key", tavilyApiKey,
                "query", query,
                "search_depth", "basic",
                "include_answer", false,
                "include_images", false,
                "max_results", maxResults
        );

        return webClient.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("results"))
                .block(); // Synchronous for simplicity; use async in production
    }
}