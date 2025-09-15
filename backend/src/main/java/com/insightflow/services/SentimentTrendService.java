package com.insightflow.services;

import com.insightflow.models.SentimentData;
import com.insightflow.repositories.SentimentRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SentimentTrendService {

    @Autowired
    private SentimentRepository repository;

    @Autowired
    private SentimentTrendCalculator calculator;

    /**
     * Analyze trends for a company over days with enhanced metrics and caching.
     * @param companyName e.g., "Tesla"
     * @param days Back days
     * @param sources Filter sources
     * @return Map with time_series, trends, significant_events, and source-specific analysis
     */
    @Cacheable(value = "sentimentTrends", key = "#companyName + '-' + #days + '-' + #sources")
    public Map<String, Object> analyzeTrends(String companyName, int days, List<String> sources) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        List<SentimentData> data = repository.findByCompanyNameAndTimestampBetween(companyName, start, LocalDateTime.now());

        // Filter by sources if specified
        if (sources != null && !sources.isEmpty()) {
            data = data.stream().filter(d -> sources.contains(d.getSourceType())).toList();
        }

        // Remove failed analyses and keep valid risk ratings
        data = data.stream()
            .filter(d -> d.getSentimentScore() > 0)  // Remove failed analyses
            .filter(d -> d.getRiskRating() >= 0)     // Keep valid risk ratings
            .toList();

        // Prepare time_series (real data points) and group by source
        List<Map<String, Object>> timeSeries = new ArrayList<>();
        Map<String, List<Double>> scoresBySource = new HashMap<>();
        
        for (SentimentData d : data) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.getTimestamp().toString());
            point.put("sentiment_score", d.getSentimentScore());
            point.put("risk_rating", d.getRiskRating());
            point.put("source", d.getSourceType());
            timeSeries.add(point);
            
            // Group scores by source for source-specific analysis
            scoresBySource.computeIfAbsent(d.getSourceType(), k -> new ArrayList<>()).add(d.getSentimentScore());
        }

        // Compute overall trends
        Map<String, Double> trends = calculator.computeTrends(data);
        
        // Compute source-specific trends
        Map<String, Map<String, Double>> sourceTrends = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : scoresBySource.entrySet()) {
            String source = entry.getKey();
            List<Double> scores = entry.getValue();
            
            // Convert to SentimentData list for the calculator
            List<SentimentData> sourceData = data.stream()
                .filter(d -> d.getSourceType().equals(source))
                .toList();
                
            if (!sourceData.isEmpty()) {
                sourceTrends.put(source, calculator.computeTrends(sourceData));
            }
        }

        // Detect significant events
        List<Map<String, Object>> events = calculator.detectSignificantEvents(data, 
            trends.get("average_score"), trends.get("volatility"));

        Map<String, Object> response = new HashMap<>();
        response.put("company_name", companyName);
        response.put("time_period_days", days);
        response.put("data_point_count", data.size());
        response.put("time_series", timeSeries);
        response.put("overall_trends", trends);
        response.put("source_specific_trends", sourceTrends);
        response.put("significant_events", events);
        response.put("analysis_timestamp", LocalDateTime.now().toString());

        return response;
    }

    /**
     * Clear cache when new sentiment data is added.
     */
    @CacheEvict(value = "sentimentTrends", allEntries = true)
    public void clearTrendsCache() {
        // This will be called when new sentiment data is added
    }
}