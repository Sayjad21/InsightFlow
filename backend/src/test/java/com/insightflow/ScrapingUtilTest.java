package com.insightflow;

import com.insightflow.utils.ScrapingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScrapingUtilTest {

    @Autowired
    private ScrapingUtil scrapingUtil;

    @Test
    public void testWebScraping() {
        String url = "https://httpbin.org/html"; // Simple test page
        
        try {
            String content = scrapingUtil.extractTextFromUrl(url);
            System.out.println("✅ Scraped content length: " + content.length());
            System.out.println("✅ First 200 chars: " + content.substring(0, Math.min(200, content.length())));
        } catch (Exception e) {
            System.out.println("❌ Scraping failed: " + e.getMessage());
        }
    }
}
