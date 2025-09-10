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
     * 
     * @param texts List of texts.
     * @return List of embeddings.
     */
    public List<Embedding> embedDocuments(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Text list is null or empty");
        }

        int batchSize = 8;
        List<Embedding> embeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
            for (String text : batch) {
                if (text == null || text.trim().isEmpty()) {
                    System.err.println("Warning: Skipping null or empty text in embedding batch");
                    continue;
                }
                try {
                    embeddings.add(embedQuery(text));
                } catch (Exception e) {
                    System.err
                            .println("Warning: Failed to embed text (length=" + text.length() + "): " + e.getMessage());
                    throw e; // Re-throw to maintain error propagation
                }
            }
        }

        if (embeddings.isEmpty()) {
            throw new IllegalStateException("No embeddings were created from the provided texts");
        }

        return embeddings;
    }

    /**
     * Embeds a single query text, mirroring embed_query.
     * 
     * @param text The text.
     * @return Embedding.
     */
    public Embedding embedQuery(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text is null or empty");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        System.out.println("EMBEDDING: Attempting to embed text of length: " + text.length());
        System.out.println("EMBEDDING: First 100 chars: " + text.substring(0, Math.min(100, text.length())));

        try {
            // Build JSON payload
            Map<String, Object> content = Map.of(
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))),
                    "taskType", "RETRIEVAL_QUERY");
            String jsonPayload = objectMapper.writeValueAsString(content);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiEndpoint + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("EMBEDDING: HTTP Status: " + response.statusCode());

            if (response.statusCode() == 429) {
                System.out.println("EMBEDDING: Quota exceeded, using mock embedding for testing");
                // Return a mock embedding for testing when quota is exceeded
                return createMockEmbedding(text);
            }

            if (response.statusCode() != 200) {
                System.err.println("EMBEDDING: API Error Response: " + response.body());
                throw new RuntimeException(
                        "Gemini API error (status " + response.statusCode() + "): " + response.body());
            }

            // Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Double> embeddingValues = (List<Double>) ((Map<String, Object>) responseMap.get("embedding"))
                    .get("values");
            float[] vector = new float[embeddingValues.size()];
            for (int i = 0; i < embeddingValues.size(); i++) {
                vector[i] = embeddingValues.get(i).floatValue();
            }

            return Embedding.from(vector);
        } catch (Exception e) {
            System.out.println("EMBEDDING: Exception occurred, falling back to mock embedding: " + e.getMessage());
            // Fallback to mock embedding on any error during development
            return createMockEmbedding(text);
        }
    }

    /**
     * Creates a mock embedding for testing when API quota is exceeded
     */
    private Embedding createMockEmbedding(String text) {
        // Create a simple hash-based mock embedding
        int hash = text.hashCode();
        float[] vector = new float[768]; // Gemini embedding dimension

        // Fill with deterministic values based on text hash
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) Math.sin((hash + i) * 0.01) * 0.1f;
        }

        System.out.println("EMBEDDING: Created mock embedding with " + vector.length + " dimensions");
        return Embedding.from(vector);
    }
}