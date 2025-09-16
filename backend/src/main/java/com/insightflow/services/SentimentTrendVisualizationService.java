package com.insightflow.services;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class SentimentTrendVisualizationService {
    private static final Logger logger = LoggerFactory.getLogger(SentimentTrendVisualizationService.class);
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    /**
     * Generate real time-series line chart and upload to Supabase.
     * 
     * @param response Contains "time_series" list
     * @return Supabase URL string or base64 fallback if Supabase fails
     */
    public String generateTrendGraph(Map<String, Object> response) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> timeSeries = (List<Map<String, Object>>) response.get("time_series");

        if (timeSeries == null || timeSeries.isEmpty()) {
            logger.warn("No time series data available for chart generation");
            return null;
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series = new TimeSeries("Sentiment Score");

        int processedPoints = 0;
        for (Map<String, Object> point : timeSeries) {
            try {
                String dateStr = (String) point.get("date");
                LocalDateTime dateTime = parseDateTime(dateStr);

                if (dateTime == null) {
                    logger.warn("Failed to parse date: {}", dateStr);
                    continue;
                }

                Object scoreObj = point.get("sentiment_score");
                double score;
                if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).doubleValue();
                } else if (scoreObj instanceof String) {
                    score = Double.parseDouble((String) scoreObj);
                } else {
                    logger.warn("Invalid score type: {}", scoreObj.getClass());
                    continue;
                }
                
                // FIX: Use Hour instead of Day for better granularity
                // Round to 3-hour intervals:
                int roundedHour = (dateTime.getHour() / 3) * 3; // Rounds to 0, 3, 6, 9, 12, 15, 18, 21
                series.add(new Hour(roundedHour, dateTime.getDayOfMonth(), 
                                  dateTime.getMonthValue(), dateTime.getYear()), score);
                processedPoints++;
            } catch (Exception e) {
                logger.warn("Failed to process data point: {}", point, e);
            }
        }

        if (processedPoints == 0) {
            logger.error("No valid data points processed for chart");
            return null;
        }

        dataset.addSeries(series);
        JFreeChart chart = createStyledChart(dataset, (String) response.get("company_name"));

        try {
            // Try to upload to Supabase first
            if (supabaseStorageService.isAvailable()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ChartUtils.writeChartAsPNG(baos, chart, 1000, 600);

                String companyName = (String) response.get("company_name");
                String fileName = generateChartFileName(companyName, "trend");

                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, fileName, "image/png");
                if (supabaseUrl != null) {
                    logger.info("Successfully uploaded trend chart to Supabase: {}", supabaseUrl);
                    return supabaseUrl;
                } else {
                    logger.warn("Failed to upload to Supabase, falling back to base64");
                }
            } else {
                logger.warn("Supabase not available, using base64 encoding");
            }

            // Fallback to base64 if Supabase fails or is unavailable
            return convertChartToBase64(chart);

        } catch (IOException e) {
            logger.error("Failed to generate chart image", e);
            return null;
        }
    }

    /**
     * Parse datetime from string using multiple formats
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // Try parsing as LocalDateTime first
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try parsing as LocalDate and convert to LocalDateTime
                try {
                    return java.time.LocalDate.parse(dateStr, formatter).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // Continue to next formatter
                }
            }
        }

        return null;
    }

    /**
     * Create a styled chart with better visuals
     */
    private JFreeChart createStyledChart(TimeSeriesCollection dataset, String companyName) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Sentiment Trend - " + companyName,
                "Date",
                "Score (0-100)",
                dataset,
                true,
                true,
                false);

        // Customize the chart appearance
        XYPlot plot = chart.getXYPlot();

        // Set background colors
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Customize the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180)); // Steel blue line
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Format date axis
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new java.text.SimpleDateFormat("MMM dd"));

        return chart;
    }

    /**
     * Convert chart to base64 PNG
     */
    private String convertChartToBase64(JFreeChart chart) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 1000, 600); // Higher resolution
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Generate comparison chart for multiple companies
     */
    public String generateComparisonChart(Map<String, Map<String, Object>> companiesData) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        Color[] colors = { Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA };

        for (Map.Entry<String, Map<String, Object>> entry : companiesData.entrySet()) {
            String company = entry.getKey();
            Map<String, Object> data = entry.getValue();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> timeSeries = (List<Map<String, Object>>) data.get("time_series");

            if (timeSeries == null || timeSeries.isEmpty()) {
                continue;
            }

            TimeSeries series = new TimeSeries(company);
            for (Map<String, Object> point : timeSeries) {
                try {
                    String dateStr = (String) point.get("date");
                    LocalDateTime dateTime = parseDateTime(dateStr);

                    if (dateTime == null)
                        continue;

                    Object scoreObj = point.get("sentiment_score");
                    double score;
                    if (scoreObj instanceof Number) {
                        score = ((Number) scoreObj).doubleValue();
                    } else if (scoreObj instanceof String) {
                        score = Double.parseDouble((String) scoreObj);
                    } else {
                        continue;
                    }
                    
                    // Use Hour for better granularity:
                    // Round to 3-hour intervals:
                    int roundedHour = (dateTime.getHour() / 3) * 3;
                    series.add(new Hour(roundedHour, dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear()), score);
                    // Or use Minute for maximum precision:
                    // series.add(new Minute(dateTime.getMinute(), dateTime.getHour(), dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear()), score);
                } catch (Exception e) {
                    logger.warn("Failed to process data point for {}: {}", company, point, e);
                }
            }

            if (series.getItemCount() > 0) {
                dataset.addSeries(series);
            }
        }

        if (dataset.getSeriesCount() == 0) {
            return null;
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Sentiment Comparison",
                "Date",
                "Score (0-100)",
                dataset,
                true,
                true,
                false);

        // Style the comparison chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colors[i % colors.length]);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(i, true);
        }
        plot.setRenderer(renderer);

        try {
            // Try to upload to Supabase first
            if (supabaseStorageService.isAvailable()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ChartUtils.writeChartAsPNG(baos, chart, 1000, 600);

                String companiesStr = String.join("-", companiesData.keySet());
                String fileName = generateChartFileName(companiesStr, "comparison");

                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, fileName, "image/png");
                if (supabaseUrl != null) {
                    logger.info("Successfully uploaded comparison chart to Supabase: {}", supabaseUrl);
                    return supabaseUrl;
                } else {
                    logger.warn("Failed to upload comparison chart to Supabase, falling back to base64");
                }
            } else {
                logger.warn("Supabase not available, using base64 encoding for comparison chart");
            }

            // Fallback to base64
            return convertChartToBase64(chart);

        } catch (IOException e) {
            logger.error("Failed to generate comparison chart", e);
            return null;
        }
    }

    /**
     * Generate unique chart filename
     */
    private String generateChartFileName(String companyName, String chartType) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedCompany = companyName.replaceAll("[^a-zA-Z0-9\\-]", "_");
        return chartType + "_" + sanitizedCompany + "_" + timestamp + ".png";
    }
}