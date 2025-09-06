package com.insightflow;

import com.insightflow.services.ScrapingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScrapingServiceTest {

    @Autowired
    private ScrapingService scrapingService;

    @Test
    public void testGetLinkedInAnalysis() {
        String companyName = "OpenAI";
        try {
            System.out.println("=== Starting LinkedIn scraping test for: " + companyName + " ===");
            String result = scrapingService.getLinkedInAnalysis(companyName);
            System.out.println("=== ScrapingService returned result ===");
            System.out.println(result);

            // Assertions
            assertNotNull(result, "Result is null");
            assertFalse(result.isBlank(), "Result is empty or blank");
            assertTrue(result.contains("<strong>Analyse LinkedIn de " + companyName + "</strong>"),
                    "Result does not contain expected header");
            assertTrue(result.contains("<br>"), "Result does not contain <br> tags");
            assertTrue(result.contains("<strong>"), "Result does not contain <strong> tags");

            // Check for LLM fallback response
            if (result.contains("I don't see any content provided") || result.contains("Please provide the LinkedIn content")) {
                fail("No real LinkedIn data was scraped. Check linkedin_openai.html and linkedin_openai_fulltext.txt for page source and content.");
            }

            // Check for OpenAI-specific content
            assertTrue(result.toLowerCase().contains("openai") || result.toLowerCase().contains("artificial intelligence") ||
                            result.toLowerCase().contains("ai research") || result.toLowerCase().contains("chatgpt"),
                    "Result does not contain OpenAI-specific content");

            System.out.println("✅ Real LinkedIn data appears to be present.");

        } catch (Exception e) {
            System.out.println("❌ Error during LinkedIn scraping:");
            e.printStackTrace();
            fail("LinkedIn analysis failed for " + companyName + ": " + e.getMessage(), e);
        }
    }
}