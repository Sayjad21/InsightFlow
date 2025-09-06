package com.insightflow.utils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeminiEmbeddingModel implements EmbeddingModel {

    @Autowired
    private EmbeddingUtil embeddingUtil;

    @Override
    public Response<Embedding> embed(String text) {
        Embedding embedding = embeddingUtil.embedQuery(text);
        return Response.from(embedding);
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        Embedding embedding = embeddingUtil.embedQuery(textSegment.text());
        return Response.from(embedding);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream().map(TextSegment::text).collect(Collectors.toList());
        List<Embedding> embeddings = embeddingUtil.embedDocuments(texts);
        return Response.from(embeddings);
    }

    @Override
    public int dimension() {
        // Gemini embedding-001 has 768 dimensions
        return 768;
    }
}