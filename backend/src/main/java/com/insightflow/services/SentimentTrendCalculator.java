package com.insightflow.services;

import com.insightflow.models.SentimentData;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.ZoneOffset;
import java.util.Comparator;

@Component
public class SentimentTrendCalculator {

    /**
     * Compute real trends: slope (regression), volatility (std dev), avg.
     */
    public Map<String, Double> computeTrends(List<SentimentData> data) {
        if (data.isEmpty()) return new HashMap<>();

        // Extract scores as double[] for math
        double[] scores = data.stream().mapToDouble(SentimentData::getSentimentScore).toArray();

        // Average
        DescriptiveStatistics stats = new DescriptiveStatistics(scores);
        double avg = stats.getMean();

        // Volatility (std dev)
        double volatility = stats.getStandardDeviation();

        // Slope (linear regression: time as x, score as y)
        SimpleRegression regression = new SimpleRegression();
        long baseTime = data.get(0).getTimestamp().toEpochSecond(ZoneOffset.UTC);  // Normalized time
        for (int i = 0; i < data.size(); i++) {
            long timeX = data.get(i).getTimestamp().toEpochSecond(ZoneOffset.UTC) - baseTime;
            regression.addData((double) timeX, scores[i]);
        }
        double slope = regression.getSlope();  // Positive = upward trend

        Map<String, Double> trends = new HashMap<>();
        trends.put("average_score", avg);
        trends.put("volatility", volatility);
        trends.put("slope", slope);

        return trends;
    }

    /**
     * Detect significant events: spikes, drops, and trend reversals.
     */
    public List<Map<String, Object>> detectSignificantEvents(List<SentimentData> data, double avg, double volatility) {
        List<Map<String, Object>> events = new ArrayList<>();
        double upperThreshold = avg + 2 * volatility;
        double lowerThreshold = avg - 2 * volatility;
        
        // Create a mutable copy of the list before sorting
        List<SentimentData> sortableData = new ArrayList<>(data);
        sortableData.sort(Comparator.comparing(SentimentData::getTimestamp));
        
        for (int i = 0; i < sortableData.size(); i++) {
            SentimentData current = sortableData.get(i);
            double score = current.getSentimentScore();
            
            // Detect spikes (both positive and negative)
            if (score > upperThreshold || score < lowerThreshold) {
                Map<String, Object> event = new HashMap<>();
                event.put("date", current.getTimestamp().toString());
                event.put("score", score);
                event.put("type", score > upperThreshold ? "POSITIVE_SPIKE" : "NEGATIVE_SPIKE");
                event.put("deviation", Math.abs(score - avg));
                event.put("source", current.getSourceType());
                events.add(event);
            }
            
            // Detect trend changes (if we have enough data)
            if (i >= 2) {
                SentimentData prev1 = sortableData.get(i-1);
                SentimentData prev2 = sortableData.get(i-2);
                
                double currentSlope = score - prev1.getSentimentScore();
                double previousSlope = prev1.getSentimentScore() - prev2.getSentimentScore();
                
                // Significant change in trend direction
                if (currentSlope * previousSlope < 0 && Math.abs(currentSlope) > volatility/2) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("date", current.getTimestamp().toString());
                    event.put("score", score);
                    event.put("type", currentSlope > 0 ? "TREND_REVERSAL_UP" : "TREND_REVERSAL_DOWN");
                    event.put("slope_change", Math.abs(currentSlope - previousSlope));
                    event.put("source", current.getSourceType());
                    events.add(event);
                }
            }
        }
        return events;
    }
}