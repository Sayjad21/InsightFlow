package com.insightflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.insightflow.services.SentimentTrendVisualizationService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SentimentTrendVisualizationServiceTest {

    @Autowired
    private SentimentTrendVisualizationService service;

    @Test
    public void testGenerateTrendGraph() throws IOException {
        // Create sample data
        Map<String, Object> response = new HashMap<>();
        response.put("company_name", "TestCompany");

        List<Map<String, Object>> timeSeries = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Add sample data points
        for (int i = 0; i < 10; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", now.minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            point.put("sentiment_score", 50 + (Math.random() * 50 - 25)); // Random score between 25-75
            timeSeries.add(point);
        }

        response.put("time_series", timeSeries);

        // Generate chart
        String base64Image = service.generateTrendGraph(response);
        assertNotNull(base64Image);

        // Save to file
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Files.write(Paths.get("trend_chart.png"), imageBytes);
    }

    @Test
    public void testGenerateComparisonChart() throws IOException {
        // Create sample data for multiple companies
        Map<String, Map<String, Object>> companiesData = new HashMap<>();
        
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> companyData = new HashMap<>();
            List<Map<String, Object>> timeSeries = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (int j = 0; j < 5; j++) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", now.minusHours(j).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                point.put("sentiment_score", 40 + (Math.random() * 40 - 20)); // Random score between 20-60
                timeSeries.add(point);
            }

            companyData.put("time_series", timeSeries);
            companiesData.put("Company" + i, companyData);
        }

        // Generate comparison chart
        String base64Image = service.generateComparisonChart(companiesData);
        assertNotNull(base64Image);

        // Save to file
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Files.write(Paths.get("comparison_chart.png"), imageBytes);
    }
}