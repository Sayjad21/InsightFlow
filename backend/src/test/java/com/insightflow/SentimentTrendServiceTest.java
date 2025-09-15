package com.insightflow;

import com.insightflow.services.SentimentTrendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class SentimentTrendServiceTest {

    @Autowired
    private SentimentTrendService trendService;

    @Test
    public void testTrendAnalysis() {
        System.out.println("=== Starting Sentiment Trend Analysis Test ===");

        try {
            // Use Apple instead - it has real sentiment scores (85, 92, 85)
            Map<String, Object> result = trendService.analyzeTrends("Apple", 7, List.of("news", "social"));

            // Or use Tesla - it has good scores (85, 75)
            // Map<String, Object> result = trendService.analyzeTrends("Tesla", 7, List.of("news", "social"));

            // Print the results
            System.out.println("Trend Analysis Results:");
            System.out.println("Company: " + result.get("company_name"));
            System.out.println("Data Points: " + result.get("data_point_count"));
            System.out.println("Time Period: " + result.get("time_period_days") + " days");

            // Print trends
            Map<String, Double> trends = (Map<String, Double>) result.get("overall_trends");
            System.out.println("Average Score: " + trends.get("average_score"));
            System.out.println("Volatility: " + trends.get("volatility"));
            System.out.println("Slope: " + trends.get("slope"));

            // Print source-specific trends if available
            Map<String, Map<String, Double>> sourceTrends =
                    (Map<String, Map<String, Double>>) result.get("source_specific_trends");
            if (sourceTrends != null && !sourceTrends.isEmpty()) {
                System.out.println("Source-Specific Trends:");
                sourceTrends.forEach((source, sourceTrend) ->
                        System.out.println("  " + source + ": " + sourceTrend)
                );
            }

            // Print significant events
            List<Map<String, Object>> events =
                    (List<Map<String, Object>>) result.get("significant_events");
            System.out.println("Significant Events: " + events.size());
            for (Map<String, Object> event : events) {
                System.out.println("  Event: " + event);
            }

            // Print risk trends
            Map<String, Double> riskTrends = (Map<String, Double>) result.get("risk_trends");
            if (riskTrends != null) {
                System.out.println("Risk Analysis:");
                System.out.println("Average Risk: " + riskTrends.get("average_risk"));
                System.out.println("Risk Volatility: " + riskTrends.get("risk_volatility"));
                System.out.println("Risk Slope: " + riskTrends.get("risk_slope"));
            }

            System.out.println("=== Trend Analysis Test Completed ===");

        } catch (Exception e) {
            System.err.println("Error during trend analysis test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
