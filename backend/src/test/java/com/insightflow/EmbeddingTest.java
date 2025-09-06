package com.insightflow;

import com.insightflow.utils.EmbeddingUtil;
import dev.langchain4j.data.embedding.Embedding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class EmbeddingTest {

    @Autowired
    private EmbeddingUtil embeddingUtil;

    @Test
    public void testEmbedding() {
        String text = "This is a test sentence.";
        
        System.out.println("Testing embedding for: " + text);
        
        try {
            Embedding embedding = embeddingUtil.embedQuery(text);
            
            System.out.println("SUCCESS!");
            System.out.println("Embedding dimension: " + embedding.vector().length);
            System.out.println("First 5 values: ");
            for (int i = 0; i < 5 && i < embedding.vector().length; i++) {
                System.out.println("  [" + i + "] = " + embedding.vector()[i]);
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}