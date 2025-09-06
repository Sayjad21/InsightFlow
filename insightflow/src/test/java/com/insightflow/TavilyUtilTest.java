package com.insightflow;

import com.insightflow.utils.TavilyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TavilyUtilTest {

    @Autowired
    private TavilyUtil tavilyUtil;

    @Test
    public void testTavilySearch() {
        try {
            List<Map<String, Object>> results = tavilyUtil.search("OpenAI latest news", 3);
            
            System.out.println("✅ Tavily search results: " + results.size() + " items");
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> result = results.get(i);
                System.out.println("  [" + i + "] " + result.get("url"));
                System.out.println("      " + result.get("content").toString().substring(0, 100) + "...");
            }
        } catch (Exception e) {
            System.out.println("❌ Tavily search failed: " + e.getMessage());
        }
    }
}
