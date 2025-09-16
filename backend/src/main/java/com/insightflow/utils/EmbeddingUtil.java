package com.insightflow.utils;

   import dev.langchain4j.data.embedding.Embedding;
   import dev.langchain4j.data.segment.TextSegment;
   import dev.langchain4j.model.output.Response;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Component;

   import java.util.ArrayList;
   import java.util.List;
   import java.util.stream.Collectors;

   @Component
   public class EmbeddingUtil {

       private final CustomOllamaEmbeddingModel embeddingModel;

       @Autowired
       public EmbeddingUtil(CustomOllamaEmbeddingModel embeddingModel) {
           this.embeddingModel = embeddingModel;
       }

       /**
        * Embeds a single query text using Ollama.
        */
       public Embedding embedQuery(String text) {
           if (text == null || text.trim().isEmpty()) {
               throw new IllegalArgumentException("Text is null or empty");
           }
           Response<Embedding> response = embeddingModel.embed(text);
           return response.content();
       }

       /**
        * Embeds multiple documents using Ollama.
        */
       public List<Embedding> embedDocuments(List<String> texts) {
           if (texts == null || texts.isEmpty()) {
               throw new IllegalArgumentException("Text list is null or empty");
           }

           int batchSize = 8; // Maintain batching for performance
           List<Embedding> allEmbeddings = new ArrayList<>();

           for (int i = 0; i < texts.size(); i += batchSize) {
               List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
               // Convert each String to a TextSegment
               List<TextSegment> textSegments = batch.stream()
                   .map(TextSegment::from)
                   .collect(Collectors.toList());
               Response<List<Embedding>> response = embeddingModel.embedAll(textSegments);
               allEmbeddings.addAll(response.content());
           }

           System.out.println("EMBEDDING: Successfully created " + allEmbeddings.size() + " embeddings");
           return allEmbeddings;
       }
   }