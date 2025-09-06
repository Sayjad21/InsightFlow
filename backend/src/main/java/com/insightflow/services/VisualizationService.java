package com.insightflow.services;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisualizationService {

    /**
     * Generates SWOT image as base64, mirroring generate_swot_image_base64 (placeholder bar chart).
     * @param swot Map from AnalysisService.
     * @return Base64 PNG.
     */
    public String generateSwotImage(Map<String, List<String>> swot) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(swot.getOrDefault("strengths", List.of()).size(), "Strengths", "Count");
            dataset.addValue(swot.getOrDefault("weaknesses", List.of()).size(), "Weaknesses", "Count");
            dataset.addValue(swot.getOrDefault("opportunities", List.of()).size(), "Opportunities", "Count");
            dataset.addValue(swot.getOrDefault("threats", List.of()).size(), "Threats", "Count");

            JFreeChart chart = ChartFactory.createBarChart(
                    "SWOT Analysis",
                    "Category",
                    "Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate SWOT image", e);
        }
    }

    /**
     * Generates PESTEL image as base64, mirroring generate_pestel_image_base64 (bar chart approximation).
     * @param pestel Map from AnalysisService.
     * @return Base64 PNG.
     */
    public String generatePestelImage(Map<String, List<String>> pestel) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(pestel.getOrDefault("political", List.of()).size(), "Political", "Count");
            dataset.addValue(pestel.getOrDefault("economic", List.of()).size(), "Economic", "Count");
            dataset.addValue(pestel.getOrDefault("social", List.of()).size(), "Social", "Count");
            dataset.addValue(pestel.getOrDefault("technological", List.of()).size(), "Technological", "Count");
            dataset.addValue(pestel.getOrDefault("environmental", List.of()).size(), "Environmental", "Count");
            dataset.addValue(pestel.getOrDefault("legal", List.of()).size(), "Legal", "Count");

            JFreeChart chart = ChartFactory.createBarChart(
                    "PESTEL Analysis",
                    "Category",
                    "Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 1200, 800);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PESTEL image", e);
        }
    }

    /**
     * Generates Porter's Five Forces image as base64, mirroring generate_porter_image (bar chart approximation).
     * @param forces Map from AnalysisService.
     * @return Base64 PNG.
     */
    public String generatePorterImage(Map<String, List<String>> forces) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(forces.getOrDefault("rivalry", List.of()).size(), "Competitive Rivalry", "Impact");
            dataset.addValue(forces.getOrDefault("new_entrants", List.of()).size(), "Threat of New Entrants", "Impact");
            dataset.addValue(forces.getOrDefault("supplier_power", List.of()).size(), "Supplier Power", "Impact");
            dataset.addValue(forces.getOrDefault("buyer_power", List.of()).size(), "Buyer Power", "Impact");
            dataset.addValue(forces.getOrDefault("substitutes", List.of()).size(), "Threat of Substitutes", "Impact");

            JFreeChart chart = ChartFactory.createBarChart(
                    "Porter's Five Forces",
                    "Force",
                    "Impact",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 1000, 1000);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Porter image", e);
        }
    }

    /**
     * Generates BCG Matrix image as base64, mirroring generate_bcg_image (scatter plot with bubbles).
     * @param products Map from AnalysisService.
     * @return Base64 PNG.
     */
    public String generateBcgImage(Map<String, Map<String, Double>> products) {
        try {
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Products");
            Map<Integer, String> indexToName = new HashMap<>();
            int idx = 0;
            for (Map.Entry<String, Map<String, Double>> entry : products.entrySet()) {
                double marketShare = entry.getValue().getOrDefault("market_share", 0.5);
                double growthRate = entry.getValue().getOrDefault("growth_rate", 5.0);
                series.add(marketShare, growthRate);
                indexToName.put(idx++, entry.getKey());
            }
            dataset.addSeries(series);

            JFreeChart chart = ChartFactory.createScatterPlot(
                    "BCG Matrix",
                    "Market Share",
                    "Growth Rate (%)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            XYPlot plot = (XYPlot) chart.getPlot();
            // Optionally set bubble renderer for size
            plot.setRenderer(new XYBubbleRenderer());

            // Add product name annotations
            for (int i = 0; i < series.getItemCount(); i++) {
                double x = series.getX(i).doubleValue();
                double y = series.getY(i).doubleValue();
                String label = indexToName.get(i);
                org.jfree.chart.annotations.XYTextAnnotation annotation = new org.jfree.chart.annotations.XYTextAnnotation(label, x, y);
                annotation.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
                plot.addAnnotation(annotation);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 1000, 800);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate BCG image", e);
        }
    }

    /**
     * Generates McKinsey 7S image as base64, mirroring generate_mckinsey_image (pie chart approximation).
     * @param model7s Map from AnalysisService.
     * @return Base64 PNG.
     */
    public String generateMckinseyImage(Map<String, String> model7s) {
        try {
            org.jfree.data.general.DefaultPieDataset dataset = new org.jfree.data.general.DefaultPieDataset();
            dataset.setValue("Strategy", .5);
            dataset.setValue("Structure", 1.5);
            dataset.setValue("Systems", .25);
            dataset.setValue("Style", 1.75);
            dataset.setValue("Staff", 1);
            dataset.setValue("Skills", 1);
            dataset.setValue("Shared Values", 1);

            JFreeChart chart = ChartFactory.createPieChart(
                    "McKinsey 7S",
                    dataset,
                    true, true, false
            );
            PiePlot plot = (PiePlot) chart.getPlot();
            // Customize colors if needed
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 1000, 1000);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate McKinsey image", e);
        }
    }
}