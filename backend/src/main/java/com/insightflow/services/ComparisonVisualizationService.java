package com.insightflow.services;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ComparisonVisualizationService {

    // @Autowired
    // private FirebaseStorageService firebaseStorageService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    public String generateRadarChart(Map<String, Object> comparisonData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Double>> metrics = (List<Map<String, Double>>) comparisonData.get("metrics");
            @SuppressWarnings("unchecked")
            List<String> companyNames = (List<String>) comparisonData.get("company_names");

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < companyNames.size(); i++) {
                Map<String, Double> companyMetrics = metrics.get(i);
                String companyName = companyNames.get(i);
                dataset.addValue(companyMetrics.get("market_share"), companyName, "Market Share");
                dataset.addValue(companyMetrics.get("growth_rate"), companyName, "Growth Rate");
                dataset.addValue(companyMetrics.get("sentiment_score") / 10, companyName, "Sentiment Score");
                dataset.addValue(companyMetrics.get("risk_rating") * 10, companyName, "Risk Rating");
            }

            SpiderWebPlot plot = new SpiderWebPlot(dataset);
            plot.setStartAngle(54);
            JFreeChart chart = new JFreeChart(
                    "Competitive Positioning Radar",
                    JFreeChart.DEFAULT_TITLE_FONT,
                    plot,
                    true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);

            // Try to upload to Supabase first, then Firebase as fallback
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "comparison_radar.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: Comparison Radar Chart");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }

            // Try Firebase as fallback
            // if (firebaseStorageService.isAvailable()) {
            // String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos,
            // "comparison_radar.png",
            // "image/png");
            // if (firebaseUrl != null) {
            // return firebaseUrl;
            // }
            // }

            // Fallback to base64 if both uploads fail
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate radar chart", e);
        }
    }

    public String generateBarGraph(Map<String, Object> comparisonData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Double>> metrics = (List<Map<String, Double>>) comparisonData.get("metrics");
            @SuppressWarnings("unchecked")
            List<String> companyNames = (List<String>) comparisonData.get("company_names");

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < companyNames.size(); i++) {
                Map<String, Double> companyMetrics = metrics.get(i);
                String companyName = companyNames.get(i);
                dataset.addValue(companyMetrics.get("market_share"), companyName, "Market Share");
                dataset.addValue(companyMetrics.get("growth_rate"), companyName, "Growth Rate");
                dataset.addValue(companyMetrics.get("sentiment_score") / 10, companyName, "Sentiment Score");
                dataset.addValue(companyMetrics.get("risk_rating") * 10, companyName, "Risk Rating");
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Bar Comparison",
                    "Metric",
                    "Value",
                    dataset);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "comparison_bar.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: Comparison Bar Chart");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }

            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            // String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos,
            // "comparison_bar.png",
            // "image/png");
            // if (firebaseUrl != null) {
            // return firebaseUrl;
            // }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate bar graph", e);
        }
    }

    public String generateScatterPlot(Map<String, Object> comparisonData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Double>> metrics = (List<Map<String, Double>>) comparisonData.get("metrics");
            @SuppressWarnings("unchecked")
            List<String> companyNames = (List<String>) comparisonData.get("company_names");

            XYSeriesCollection dataset = new XYSeriesCollection();
            for (int i = 0; i < companyNames.size(); i++) {
                XYSeries series = new XYSeries(companyNames.get(i));
                Map<String, Double> companyMetrics = metrics.get(i);
                series.add(companyMetrics.get("growth_rate"), companyMetrics.get("market_share"));
                dataset.addSeries(series);
            }

            JFreeChart chart = ChartFactory.createScatterPlot(
                    "Scatter Positioning",
                    "Growth Rate",
                    "Market Share",
                    dataset);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "comparison_scatter.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: Comparison Scatter Plot");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }

            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            // String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos,
            // "comparison_scatter.png",
            // "image/png");
            // if (firebaseUrl != null) {
            // return firebaseUrl;
            // }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate scatter plot", e);
        }
    }
}