package com.insightflow.utils;

   import dev.langchain4j.data.embedding.Embedding;
   import dev.langchain4j.data.segment.TextSegment;
   import dev.langchain4j.model.embedding.EmbeddingModel;
   import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
   import dev.langchain4j.model.output.Response;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.stereotype.Component;

   import java.util.List;
   import java.util.stream.Collectors;

@Component
public class CustomOllamaEmbeddingModel implements EmbeddingModel {

    private final dev.langchain4j.model.ollama.OllamaEmbeddingModel ollamaModel;

    public CustomOllamaEmbeddingModel(@Value("${ollama.base.url:http://localhost:11434}") String baseUrl,
                               @Value("${ollama.embedding.model:nomic-embed-text}") String modelName) {
        this.ollamaModel = dev.langchain4j.model.ollama.OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

       @Override
       public Response<Embedding> embed(String text) {
           if (text == null || text.trim().isEmpty()) {
               throw new IllegalArgumentException("Text is null or empty");
           }
           System.out.println("EMBEDDING: Generating embedding for text length: " + text.length());
           return ollamaModel.embed(text);
       }

       @Override
       public Response<Embedding> embed(TextSegment textSegment) {
           return embed(textSegment.text());
       }

       @Override
       public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
           if (textSegments == null || textSegments.isEmpty()) {
               throw new IllegalArgumentException("Text segments list is null or empty");
           }
           System.out.println("EMBEDDING: Generating embeddings for " + textSegments.size() + " texts");
           return ollamaModel.embedAll(textSegments);
       }

       @Override
       public int dimension() {
           return 768; // Matches nomic-embed-text
       }
   }