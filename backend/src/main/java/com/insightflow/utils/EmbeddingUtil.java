package com.insightflow.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EmbeddingUtil {

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geminiEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent";

    public EmbeddingUtil(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Embeds a list of texts, mirroring embed_documents (batch size 8).
     * @param texts List of texts.
     * @return List of embeddings.
     */
    public List<Embedding> embedDocuments(List<String> texts) {
        int batchSize = 8;
        List<Embedding> embeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
            for (String text : batch) {
                embeddings.add(embedQuery(text));
            }
        }
        return embeddings;
    }

    /**
     * Embeds a single query text, mirroring embed_query.
     * @param text The text.
     * @return Embedding.
     */
    public Embedding embedQuery(String text) {
        try {
            // Build JSON payload
            Map<String, Object> content = Map.of(
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    ),
                    "taskType", "RETRIEVAL_QUERY"
            );
            String jsonPayload = objectMapper.writeValueAsString(content);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiEndpoint + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API error: " + response.body());
            }

            // Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Double> embeddingValues = (List<Double>) ((Map<String, Object>) responseMap.get("embedding")).get("values");
            float[] vector = new float[embeddingValues.size()];
            for (int i = 0; i < embeddingValues.size(); i++) {
                vector[i] = embeddingValues.get(i).floatValue();
            }

            return Embedding.from(vector);
        } catch (Exception e) {
            throw new RuntimeException("Failed to embed text: " + text, e);
        }
    }
}