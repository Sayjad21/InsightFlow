package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonService {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonService.class);

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

            double avgMarketShare = 0.0;
            double avgGrowthRate = 0.0;

            if (bcg != null && !bcg.isEmpty()) {
                avgMarketShare = bcg.values().stream()
                        .mapToDouble(m -> {
                            if (m == null)
                                return 0.0;
                            // Try both snake_case and camelCase keys
                            Double marketShare = m.get("market_share");
                            if (marketShare == null) {
                                marketShare = m.get("marketShare");
                            }
                            return marketShare != null ? marketShare : 0.0;
                        })
                        .average().orElse(0.0);
                avgGrowthRate = bcg.values().stream()
                        .mapToDouble(m -> {
                            if (m == null)
                                return 0.0;
                            // Try both snake_case and camelCase keys
                            Double growthRate = m.get("growth_rate");
                            if (growthRate == null) {
                                growthRate = m.get("growthRate");
                            }
                            return growthRate != null ? growthRate : 0.0;
                        })
                        .average().orElse(0.0);
            }

            Map<String, Double> metrics = new HashMap<>();
            metrics.put("market_share", avgMarketShare);
            metrics.put("growth_rate", avgGrowthRate);
            metrics.put("sentiment_score", scores.get("sentiment_score"));
            metrics.put("risk_rating", scores.get("risk_rating"));

            companyMetrics.add(metrics);
        }

        Map<String, Double> benchmarks = new HashMap<>();
        benchmarks.put("avg_market_share",
                companyMetrics.stream().mapToDouble(m -> m.get("market_share")).average().orElse(0.0));
        benchmarks.put("avg_growth_rate",
                companyMetrics.stream().mapToDouble(m -> m.get("growth_rate")).average().orElse(0.0));
        benchmarks.put("avg_sentiment_score",
                companyMetrics.stream().mapToDouble(m -> m.get("sentiment_score")).average().orElse(0.0));
        benchmarks.put("avg_risk_rating",
                companyMetrics.stream().mapToDouble(m -> m.get("risk_rating")).average().orElse(0.0));

        List<String> insights = generateInsights(analyses, benchmarks, companyMetrics);

        // Generate investment recommendations using Ollama AI
        String investmentRecommendations = generateInvestmentRecommendations(companyNames, benchmarks, companyMetrics);

        result.put("metrics", companyMetrics);
        result.put("benchmarks", benchmarks);
        result.put("insights", insights);
        result.put("investment_recommendations", investmentRecommendations);
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
                if (i > 0)
                    text.append(", ");
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
        String[] negativeWords = { "risk", "threat", "challenge", "problem", "issue", "weakness", "decline", "loss",
                "fail" };
        return countOccurrences(text, negativeWords);
    }

    private int countPositiveWords(String text) {
        String[] positiveWords = { "growth", "opportunity", "strength", "success", "profit", "gain", "advantage",
                "leadership" };
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

    private List<String> generateInsights(List<Map<String, Object>> analyses, Map<String, Double> benchmarks,
            List<Map<String, Double>> companyMetrics) {
        List<String> insights = new ArrayList<>();

        // Add BCG Matrix product-level insights
        insights.addAll(generateBcgInsights(analyses));

        for (int i = 0; i < analyses.size(); i++) {
            String companyName = (String) analyses.get(i).get("company_name");
            Map<String, Double> metrics = companyMetrics.get(i);

            // Growth insights - lowered threshold from 5 to 2
            if (metrics.get("growth_rate") > benchmarks.get("avg_growth_rate") + 2) {
                insights.add(companyName + " shows above-average growth (" +
                        String.format("%.1f", metrics.get("growth_rate")) + "% vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_growth_rate"))
                        + "%), indicating strong market positioning.");
            } else if (metrics.get("growth_rate") < benchmarks.get("avg_growth_rate") - 2) {
                insights.add(companyName + " shows below-average growth (" +
                        String.format("%.1f", metrics.get("growth_rate")) + "% vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_growth_rate"))
                        + "%), which may indicate market challenges.");
            } else if (Math.abs(metrics.get("growth_rate") - benchmarks.get("avg_growth_rate")) < 0.1) {
                insights.add(companyName + " has growth rate aligned with industry average (" +
                        String.format("%.1f", metrics.get("growth_rate"))
                        + "%), suggesting stable market performance.");
            }

            // Sentiment insights - lowered threshold from 15 to 7
            if (metrics.get("sentiment_score") > benchmarks.get("avg_sentiment_score") + 7) {
                insights.add(companyName + " has positive market sentiment (" +
                        String.format("%.0f", metrics.get("sentiment_score")) + "/100 vs industry avg " +
                        String.format("%.0f", benchmarks.get("avg_sentiment_score"))
                        + "/100), suggesting strong investor confidence.");
            } else if (metrics.get("sentiment_score") < benchmarks.get("avg_sentiment_score") - 7) {
                insights.add(companyName + " has below-average market sentiment (" +
                        String.format("%.0f", metrics.get("sentiment_score")) + "/100 vs industry avg " +
                        String.format("%.0f", benchmarks.get("avg_sentiment_score"))
                        + "/100), which may warrant investigation.");
            }

            // Risk insights - lowered threshold from 2 to 1
            if (metrics.get("risk_rating") > benchmarks.get("avg_risk_rating") + 1) {
                insights.add(companyName + " carries higher-than-average risk (" +
                        String.format("%.1f", metrics.get("risk_rating")) + "/10 vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_risk_rating"))
                        + "/10), requiring careful risk management strategies.");
            } else if (metrics.get("risk_rating") < benchmarks.get("avg_risk_rating") - 1) {
                insights.add(companyName + " appears to be lower risk than peers (" +
                        String.format("%.1f", metrics.get("risk_rating")) + "/10 vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_risk_rating"))
                        + "/10), indicating stable operations.");
            }

            // Market share insights - new addition
            if (metrics.get("market_share") > benchmarks.get("avg_market_share") + 0.3) {
                insights.add(companyName + " has above-average market share (" +
                        String.format("%.1f", metrics.get("market_share")) + "% vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_market_share"))
                        + "%), indicating strong market presence.");
            } else if (metrics.get("market_share") < benchmarks.get("avg_market_share") - 0.3) {
                insights.add(companyName + " has below-average market share (" +
                        String.format("%.1f", metrics.get("market_share")) + "% vs industry avg " +
                        String.format("%.1f", benchmarks.get("avg_market_share"))
                        + "%), suggesting opportunities for market expansion.");
            }
        }

        // Add comparative insights
        if (analyses.size() == 2) {
            insights.addAll(generateComparativeInsights(analyses, companyMetrics));
        }

        return insights;
    }

    private List<String> generateBcgInsights(List<Map<String, Object>> analyses) {
        List<String> insights = new ArrayList<>();

        for (Map<String, Object> analysis : analyses) {
            String companyName = (String) analysis.get("company_name");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Double>> bcg = (Map<String, Map<String, Double>>) analysis.get("bcg_matrix");

            if (bcg != null && !bcg.isEmpty()) {
                // Find best and worst performing products
                String bestProduct = null;
                String worstProduct = null;
                double highestGrowth = -1;
                double lowestGrowth = Double.MAX_VALUE;

                for (Map.Entry<String, Map<String, Double>> entry : bcg.entrySet()) {
                    String product = entry.getKey();
                    Map<String, Double> metrics = entry.getValue();

                    if (metrics != null) {
                        Double growthRate = metrics.get("growthRate");
                        if (growthRate == null) {
                            growthRate = metrics.get("growth_rate");
                        }

                        if (growthRate != null) {
                            if (growthRate > highestGrowth) {
                                highestGrowth = growthRate;
                                bestProduct = product;
                            }
                            if (growthRate < lowestGrowth) {
                                lowestGrowth = growthRate;
                                worstProduct = product;
                            }
                        }
                    }
                }

                if (bestProduct != null) {
                    insights.add(companyName + "'s " + bestProduct + " shows the highest growth potential at " +
                            String.format("%.1f", highestGrowth) + "%, making it a key growth driver.");
                }

                if (worstProduct != null && !worstProduct.equals(bestProduct)) {
                    insights.add(companyName + "'s " + worstProduct + " has the lowest growth rate at " +
                            String.format("%.1f", lowestGrowth) + "%, potentially requiring strategic attention.");
                }
            }
        }

        return insights;
    }

    private List<String> generateComparativeInsights(List<Map<String, Object>> analyses,
            List<Map<String, Double>> companyMetrics) {
        List<String> insights = new ArrayList<>();

        if (analyses.size() != 2 || companyMetrics.size() != 2) {
            return insights;
        }

        String company1 = (String) analyses.get(0).get("company_name");
        String company2 = (String) analyses.get(1).get("company_name");
        Map<String, Double> metrics1 = companyMetrics.get(0);
        Map<String, Double> metrics2 = companyMetrics.get(1);

        // Sentiment comparison
        double sentimentDiff = metrics1.get("sentiment_score") - metrics2.get("sentiment_score");
        if (Math.abs(sentimentDiff) >= 5) {
            String leader = sentimentDiff > 0 ? company1 : company2;
            String follower = sentimentDiff > 0 ? company2 : company1;
            insights.add(leader + " has higher market sentiment than " + follower + " (" +
                    String.format("%.0f vs %.0f",
                            sentimentDiff > 0 ? metrics1.get("sentiment_score") : metrics2.get("sentiment_score"),
                            sentimentDiff > 0 ? metrics2.get("sentiment_score") : metrics1.get("sentiment_score"))
                    +
                    " points), indicating better market perception.");
        }

        // Risk comparison
        double riskDiff = metrics1.get("risk_rating") - metrics2.get("risk_rating");
        if (Math.abs(riskDiff) >= 0.5) {
            String safer = riskDiff < 0 ? company1 : company2;
            String riskier = riskDiff < 0 ? company2 : company1;
            insights.add(safer + " appears less risky than " + riskier + " (" +
                    String.format("%.1f vs %.1f risk rating",
                            riskDiff < 0 ? metrics1.get("risk_rating") : metrics2.get("risk_rating"),
                            riskDiff < 0 ? metrics2.get("risk_rating") : metrics1.get("risk_rating"))
                    +
                    "), suggesting more stable operations.");
        }

        return insights;
    }

    private String generateInvestmentRecommendations(List<String> companyNames,
            Map<String, Double> benchmarks, List<Map<String, Double>> companyMetrics) {
        try {
            // Format the metrics data for AI analysis
            StringBuilder metricsData = new StringBuilder();
            metricsData.append("COMPARATIVE ANALYSIS DATA:\n\n");

            // Add benchmark information
            metricsData.append("Industry Benchmarks:\n");
            metricsData.append("- Average Market Share: ")
                    .append(String.format("%.2f%%", benchmarks.get("avg_market_share"))).append("\n");
            metricsData.append("- Average Growth Rate: ")
                    .append(String.format("%.2f%%", benchmarks.get("avg_growth_rate"))).append("\n");
            metricsData.append("- Average Risk Rating: ")
                    .append(String.format("%.2f/10", benchmarks.get("avg_risk_rating"))).append("\n");
            metricsData.append("- Average Sentiment Score: ")
                    .append(String.format("%.1f/100", benchmarks.get("avg_sentiment_score"))).append("\n\n");

            // Add individual company metrics
            metricsData.append("Individual Company Metrics:\n");
            for (int i = 0; i < companyNames.size(); i++) {
                String companyName = companyNames.get(i);
                Map<String, Double> metrics = companyMetrics.get(i);

                metricsData.append(String.format("%d. %s:\n", i + 1, companyName.toUpperCase()));
                metricsData.append("   - Market Share: ").append(String.format("%.2f%%", metrics.get("market_share")))
                        .append("\n");
                metricsData.append("   - Growth Rate: ").append(String.format("%.2f%%", metrics.get("growth_rate")))
                        .append("\n");
                metricsData.append("   - Risk Rating: ").append(String.format("%.1f/10", metrics.get("risk_rating")))
                        .append("\n");
                metricsData.append("   - Sentiment Score: ")
                        .append(String.format("%.1f/100", metrics.get("sentiment_score"))).append("\n");

                // Add performance vs benchmark
                metricsData.append("   - Performance vs Benchmark:\n");
                metricsData.append("     * Market Share: ").append(
                        getPerformanceIndicator(metrics.get("market_share"), benchmarks.get("avg_market_share")))
                        .append("\n");
                metricsData.append("     * Growth Rate: ")
                        .append(getPerformanceIndicator(metrics.get("growth_rate"), benchmarks.get("avg_growth_rate")))
                        .append("\n");
                metricsData.append("     * Risk Rating: ")
                        .append(getRiskIndicator(metrics.get("risk_rating"), benchmarks.get("avg_risk_rating")))
                        .append("\n");
                metricsData.append("     * Sentiment: ").append(
                        getPerformanceIndicator(metrics.get("sentiment_score"), benchmarks.get("avg_sentiment_score")))
                        .append("\n\n");
            }

            // Use AI to generate investment recommendations with extended timeout
            Map<String, Object> variables = new HashMap<>();
            variables.put("metrics_data", metricsData.toString());

            return aiUtil.invokeWithTemplateExtended(aiUtil.getInvestmentRecommendationTemplate(), variables);

        } catch (Exception e) {
            logger.error("Failed to generate investment recommendations: {}", e.getMessage());
            return "Investment recommendations could not be generated at this time due to a technical issue. " +
                    "Please analyze the metrics manually for investment insights.";
        }
    }

    private String getPerformanceIndicator(double actual, double benchmark) {
        double diff = actual - benchmark;
        double percentage = Math.abs(diff / benchmark) * 100;

        if (Math.abs(diff) < 0.1) {
            return "At benchmark level";
        } else if (diff > 0) {
            return String.format("Above benchmark (+%.1f%%)", percentage);
        } else {
            return String.format("Below benchmark (-%.1f%%)", percentage);
        }
    }

    private String getRiskIndicator(double actual, double benchmark) {
        double diff = actual - benchmark;
        double percentage = Math.abs(diff / benchmark) * 100;

        if (Math.abs(diff) < 0.1) {
            return "At benchmark level";
        } else if (diff > 0) {
            return String.format("Higher risk (+%.1f%%)", percentage);
        } else {
            return String.format("Lower risk (-%.1f%%)", percentage);
        }
    }
}