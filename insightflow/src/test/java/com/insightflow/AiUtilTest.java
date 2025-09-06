package com.insightflow;

import com.insightflow.utils.AiUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class AiUtilTest {

    @Autowired
    private AiUtil aiUtil;

    @Test
    public void testSimplePrompt() {
        System.out.println("Testing Ollama connection...");
        System.out.println("Base URL: http://localhost:11434");
        System.out.println("Model: llama3.2:latest");
        
        try {
            String response = aiUtil.invoke("Say hello in one word");
            System.out.println("✅ Simple prompt response: " + response);
        } catch (Exception e) {
            System.out.println("❌ Error class: " + e.getClass().getSimpleName());
            System.out.println("❌ Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("❌ Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    @Test
    public void testTemplatePrompt() {
        try {
            String template = "Hello {name}, welcome to {company}!";
            Map<String, Object> variables = Map.of(
                "name", "John",
                "company", "InsightFlow"
            );
            
            String response = aiUtil.invokeWithTemplate(template, variables);
            System.out.println("✅ Template response: " + response);
        } catch (Exception e) {
            System.out.println("❌ Template test failed: " + e.getMessage());
        }
    }
}
