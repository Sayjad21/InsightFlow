package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComparisonService {

    @Autowired
    private AiUtil aiUtil;

    public Map<String, Object> computeComparison(List<Map<String, Object>> analyses) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Double>> companyMetrics = new ArrayList<>();
        List<String> companyNames = new ArrayList<>(); // Add this line
        
        for (Map<String, Object> analysis : analyses) {
            String companyName = (String) analysis.get("company_name");
            companyNames.add(companyName); // Add this line
            
            // Extract data for analysis
            String analysisText = prepareAnalysisText(analysis);
            
            // Get combined sentiment and risk scores
            Map<String, Double> scores = calculateScores(companyName, analysisText);
            
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Double>> bcg = (Map<String, Map<String, Double>>) analysis.get("bcg_matrix");
            
            double avgMarketShare = bcg.values().stream()
                    .mapToDouble(m -> m.getOrDefault("market_share", 0.0))
                    .average().orElse(0.0);
            double avgGrowthRate = bcg.values().stream()
                    .mapToDouble(m -> m.getOrDefault("growth_rate", 0.0))
                    .average().orElse(0.0);

            Map<String, Double> metrics = new HashMap<>();
            metrics.put("market_share", avgMarketShare);
            metrics.put("growth_rate", avgGrowthRate);
            metrics.put("sentiment_score", scores.get("sentiment_score"));
            metrics.put("risk_rating", scores.get("risk_rating"));

            companyMetrics.add(metrics);
        }

        Map<String, Double> benchmarks = new HashMap<>();
        benchmarks.put("avg_market_share", companyMetrics.stream().mapToDouble(m -> m.get("market_share")).average().orElse(0.0));
        benchmarks.put("avg_growth_rate", companyMetrics.stream().mapToDouble(m -> m.get("growth_rate")).average().orElse(0.0));
        benchmarks.put("avg_sentiment_score", companyMetrics.stream().mapToDouble(m -> m.get("sentiment_score")).average().orElse(0.0));
        benchmarks.put("avg_risk_rating", companyMetrics.stream().mapToDouble(m -> m.get("risk_rating")).average().orElse(0.0));

        List<String> insights = generateInsights(analyses, benchmarks, companyMetrics);

        result.put("metrics", companyMetrics);
        result.put("benchmarks", benchmarks);
        result.put("insights", insights);
        result.put("company_names", companyNames); // Add this line

        return result;
    }

    public String prepareAnalysisText(Map<String, Object> analysis) {
        StringBuilder text = new StringBuilder();
        
        // Add summaries
        @SuppressWarnings("unchecked")
        List<String> summaries = (List<String>) analysis.get("summaries");
        if (summaries != null && !summaries.isEmpty()) {
            text.append("Company Summaries:\n");
            for (String summary : summaries) {
                text.append("- ").append(summary).append("\n");
            }
            text.append("\n");
        }
        
        // Add SWOT analysis
        @SuppressWarnings("unchecked")
        Map<String, List<String>> swot = (Map<String, List<String>>) analysis.get("swot_lists");
        if (swot != null) {
            text.append("SWOT Analysis:\n");
            appendAnalysisSection(text, "Strengths", swot.get("strengths"));
            appendAnalysisSection(text, "Weaknesses", swot.get("weaknesses"));
            appendAnalysisSection(text, "Opportunities", swot.get("opportunities"));
            appendAnalysisSection(text, "Threats", swot.get("threats"));
            text.append("\n");
        }
        
        // Add PESTEL analysis
        @SuppressWarnings("unchecked")
        Map<String, List<String>> pestel = (Map<String, List<String>>) analysis.get("pestel_lists");
        if (pestel != null) {
            text.append("PESTEL Analysis:\n");
            appendAnalysisSection(text, "Political", pestel.get("political"));
            appendAnalysisSection(text, "Economic", pestel.get("economic"));
            appendAnalysisSection(text, "Social", pestel.get("social"));
            appendAnalysisSection(text, "Technological", pestel.get("technological"));
            appendAnalysisSection(text, "Environmental", pestel.get("environmental"));
            appendAnalysisSection(text, "Legal", pestel.get("legal"));
            text.append("\n");
        }
        
        // Add LinkedIn analysis
        String linkedinAnalysis = (String) analysis.get("linkedin_analysis");
        if (linkedinAnalysis != null && !linkedinAnalysis.trim().isEmpty()) {
            text.append("LinkedIn Intelligence:\n").append(linkedinAnalysis).append("\n\n");
        }
        
        return text.toString();
    }
    
    private void appendAnalysisSection(StringBuilder text, String sectionName, List<String> items) {
        if (items != null && !items.isEmpty()) {
            text.append(sectionName).append(": ");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) text.append(", ");
                text.append(items.get(i));
            }
            text.append("\n");
        }
    }

    private Map<String, Double> calculateScores(String companyName, String analysisText) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("sentiment_score", 50.0); // Default
        scores.put("risk_rating", 5.0); // Default
        
        try {
            // Use the combined analysis template for efficiency
            Map<String, Object> variables = new HashMap<>();
            variables.put("company_name", companyName);
            variables.put("information", analysisText);
            
            String response = aiUtil.invokeWithTemplate(aiUtil.getCombinedAnalysisTemplate(), variables);
            
            // Parse the JSON response
            Map<String, Object> result = aiUtil.parseJsonToMap(response);
            
            if (result.containsKey("sentiment_score")) {
                scores.put("sentiment_score", Double.parseDouble(result.get("sentiment_score").toString()));
            }
            if (result.containsKey("risk_rating")) {
                scores.put("risk_rating", Double.parseDouble(result.get("risk_rating").toString()));
            }
        } catch (Exception e) {
            // Fallback to individual calculations if combined fails
            scores.put("sentiment_score", calculateSentimentScore(companyName, analysisText));
            scores.put("risk_rating", calculateRiskRating(companyName, analysisText));
        }
        
        return scores;
    }

    private double calculateSentimentScore(String companyName, String analysisText) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("company_name", companyName);
            variables.put("information", analysisText);
            
            String response = aiUtil.invokeWithTemplate(aiUtil.getSentimentAnalysisTemplate(), variables);
            return Double.parseDouble(response.trim());
        } catch (Exception e) {
            // Fallback to a simple calculation based on text length and keyword analysis
            return Math.min(100, Math.max(0, 50 + (analysisText.length() / 1000) - 
                (countNegativeWords(analysisText) * 5) + 
                (countPositiveWords(analysisText) * 3)));
        }
    }

    private double calculateRiskRating(String companyName, String analysisText) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("company_name", companyName);
            variables.put("information", analysisText);
            
            String response = aiUtil.invokeWithTemplate(aiUtil.getRiskAssessmentTemplate(), variables);
            return Double.parseDouble(response.trim());
        } catch (Exception e) {
            // Fallback to a simple calculation based on threat count and negative words
            int threatCount = countOccurrences(analysisText, "threat") + 
                             countOccurrences(analysisText, "risk") +
                             countOccurrences(analysisText, "challenge");
            return Math.min(10, Math.max(0, threatCount / 2.0));
        }
    }
    
    private int countNegativeWords(String text) {
        String[] negativeWords = {"risk", "threat", "challenge", "problem", "issue", "weakness", "decline", "loss", "fail"};
        return countOccurrences(text, negativeWords);
    }
    
    private int countPositiveWords(String text) {
        String[] positiveWords = {"growth", "opportunity", "strength", "success", "profit", "gain", "advantage", "leadership"};
        return countOccurrences(text, positiveWords);
    }
    
    private int countOccurrences(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            count += countOccurrences(text, word);
        }
        return count;
    }
    
    private int countOccurrences(String text, String word) {
        return text.toLowerCase().split("\\b" + word.toLowerCase() + "\\b").length - 1;
    }

    private List<String> generateInsights(List<Map<String, Object>> analyses, Map<String, Double> benchmarks, List<Map<String, Double>> companyMetrics) {
        List<String> insights = new ArrayList<>();
        for (int i = 0; i < analyses.size(); i++) {
            String companyName = (String) analyses.get(i).get("company_name");
            Map<String, Double> metrics = companyMetrics.get(i);
            
            // Growth insights
            if (metrics.get("growth_rate") > benchmarks.get("avg_growth_rate") + 5) {
                insights.add(companyName + " shows significantly above-average growth (" + 
                    String.format("%.1f", metrics.get("growth_rate")) + "% vs industry avg " + 
                    String.format("%.1f", benchmarks.get("avg_growth_rate")) + "%), indicating strong market positioning.");
            } else if (metrics.get("growth_rate") < benchmarks.get("avg_growth_rate") - 5) {
                insights.add(companyName + " shows below-average growth (" + 
                    String.format("%.1f", metrics.get("growth_rate")) + "% vs industry avg " + 
                    String.format("%.1f", benchmarks.get("avg_growth_rate")) + "%), which may indicate market challenges.");
            }
            
            // Sentiment insights
            if (metrics.get("sentiment_score") > benchmarks.get("avg_sentiment_score") + 15) {
                insights.add(companyName + " has very positive market sentiment (" + 
                    String.format("%.0f", metrics.get("sentiment_score")) + "/100 vs industry avg " + 
                    String.format("%.0f", benchmarks.get("avg_sentiment_score")) + "/100), suggesting strong investor confidence.");
            } else if (metrics.get("sentiment_score") < benchmarks.get("avg_sentiment_score") - 15) {
                insights.add(companyName + " has concerning market sentiment (" + 
                    String.format("%.0f", metrics.get("sentiment_score")) + "/100 vs industry avg " + 
                    String.format("%.0f", benchmarks.get("avg_sentiment_score")) + "/100), which may warrant further investigation.");
            }
            
            // Risk insights
            if (metrics.get("risk_rating") > benchmarks.get("avg_risk_rating") + 2) {
                insights.add(companyName + " carries higher-than-average risk (" + 
                    String.format("%.1f", metrics.get("risk_rating")) + "/10 vs industry avg " + 
                    String.format("%.1f", benchmarks.get("avg_risk_rating")) + "/10), requiring careful risk management strategies.");
            } else if (metrics.get("risk_rating") < benchmarks.get("avg_risk_rating") - 2) {
                insights.add(companyName + " appears to be lower risk than peers (" + 
                    String.format("%.1f", metrics.get("risk_rating")) + "/10 vs industry avg " + 
                    String.format("%.1f", benchmarks.get("avg_risk_rating")) + "/10), indicating stable operations.");
            }
        }
        return insights;
    }
}