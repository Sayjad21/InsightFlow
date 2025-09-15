package com.insightflow.utils;

import com.insightflow.models.UserAnalysis;
import java.util.*;

/**
 * Utility class for converting UserAnalysis objects to comparison format
 * and handling analysis data transformations.
 */
public class AnalysisConversionUtil {

    /**
     * Converts a UserAnalysis object to the format expected by the comparison
     * service.
     * 
     * @param analysis The UserAnalysis object to convert
     * @return Map containing the analysis data in comparison format
     */
    public static Map<String, Object> convertToComparisonFormat(UserAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("UserAnalysis cannot be null");
        }

        Map<String, Object> result = new HashMap<>();

        // Basic information
        result.put("company_name", analysis.getCompanyName());
        result.put("summaries", analysis.getSummaries() != null ? analysis.getSummaries() : new ArrayList<>());
        result.put("sources", analysis.getSources() != null ? analysis.getSources() : new ArrayList<>());
        result.put("strategy_recommendations",
                analysis.getStrategyRecommendations() != null ? analysis.getStrategyRecommendations() : "");

        // Convert SWOT analysis
        result.put("swot_lists", convertSwotLists(analysis.getSwotLists()));

        // Convert PESTEL analysis
        result.put("pestel_lists", convertPestelLists(analysis.getPestelLists()));

        // Convert Porter Forces
        result.put("porter_forces", convertPorterForces(analysis.getPorterForces()));

        // Convert BCG Matrix
        result.put("bcg_matrix", convertBcgMatrix(analysis.getBcgMatrix()));

        // Convert McKinsey 7S
        result.put("mckinsey_7s", convertMckinsey7s(analysis.getMckinsey7s()));

        // LinkedIn analysis
        result.put("linkedin_analysis", analysis.getLinkedinAnalysis() != null ? analysis.getLinkedinAnalysis() : "");

        // Images (Base64 encoded)
        result.put("swot_image", analysis.getSwotImage() != null ? analysis.getSwotImage() : "");
        result.put("pestel_image", analysis.getPestelImage() != null ? analysis.getPestelImage() : "");
        result.put("porter_image", analysis.getPorterImage() != null ? analysis.getPorterImage() : "");
        result.put("bcg_image", analysis.getBcgImage() != null ? analysis.getBcgImage() : "");
        result.put("mckinsey_image", analysis.getMckinseyImage() != null ? analysis.getMckinseyImage() : "");

        return result;
    }

    /**
     * Converts SwotLists to the format expected by comparison service.
     */
    private static Map<String, List<String>> convertSwotLists(UserAnalysis.SwotLists swot) {
        Map<String, List<String>> result = new HashMap<>();

        if (swot != null) {
            result.put("strengths", swot.getStrengths() != null ? swot.getStrengths() : new ArrayList<>());
            result.put("weaknesses", swot.getWeaknesses() != null ? swot.getWeaknesses() : new ArrayList<>());
            result.put("opportunities", swot.getOpportunities() != null ? swot.getOpportunities() : new ArrayList<>());
            result.put("threats", swot.getThreats() != null ? swot.getThreats() : new ArrayList<>());
        } else {
            result.put("strengths", new ArrayList<>());
            result.put("weaknesses", new ArrayList<>());
            result.put("opportunities", new ArrayList<>());
            result.put("threats", new ArrayList<>());
        }

        return result;
    }

    /**
     * Converts PestelLists to the format expected by comparison service.
     */
    private static Map<String, List<String>> convertPestelLists(UserAnalysis.PestelLists pestel) {
        Map<String, List<String>> result = new HashMap<>();

        if (pestel != null) {
            result.put("political", pestel.getPolitical() != null ? pestel.getPolitical() : new ArrayList<>());
            result.put("economic", pestel.getEconomic() != null ? pestel.getEconomic() : new ArrayList<>());
            result.put("social", pestel.getSocial() != null ? pestel.getSocial() : new ArrayList<>());
            result.put("technological",
                    pestel.getTechnological() != null ? pestel.getTechnological() : new ArrayList<>());
            result.put("environmental",
                    pestel.getEnvironmental() != null ? pestel.getEnvironmental() : new ArrayList<>());
            result.put("legal", pestel.getLegal() != null ? pestel.getLegal() : new ArrayList<>());
        } else {
            result.put("political", new ArrayList<>());
            result.put("economic", new ArrayList<>());
            result.put("social", new ArrayList<>());
            result.put("technological", new ArrayList<>());
            result.put("environmental", new ArrayList<>());
            result.put("legal", new ArrayList<>());
        }

        return result;
    }

    /**
     * Generates an empty PESTEL structure as fallback.
     */
    @SuppressWarnings("unused")
    private static Map<String, List<String>> generateEmptyPestelStructure() {
        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", new ArrayList<>());
        pestel.put("economic", new ArrayList<>());
        pestel.put("social", new ArrayList<>());
        pestel.put("technological", new ArrayList<>());
        pestel.put("environmental", new ArrayList<>());
        pestel.put("legal", new ArrayList<>());
        return pestel;
    }

    /**
     * Converts Porter Forces to the format expected by comparison service.
     */
    private static Map<String, List<String>> convertPorterForces(UserAnalysis.PorterForces porter) {
        Map<String, List<String>> result = new HashMap<>();

        if (porter != null) {
            result.put("rivalry", porter.getRivalry() != null ? porter.getRivalry() : new ArrayList<>());
            result.put("new_entrants", porter.getNewEntrants() != null ? porter.getNewEntrants() : new ArrayList<>());
            result.put("substitutes", porter.getSubstitutes() != null ? porter.getSubstitutes() : new ArrayList<>());
            result.put("buyer_power", porter.getBuyerPower() != null ? porter.getBuyerPower() : new ArrayList<>());
            result.put("supplier_power",
                    porter.getSupplierPower() != null ? porter.getSupplierPower() : new ArrayList<>());
        } else {
            result.put("rivalry", new ArrayList<>());
            result.put("new_entrants", new ArrayList<>());
            result.put("substitutes", new ArrayList<>());
            result.put("buyer_power", new ArrayList<>());
            result.put("supplier_power", new ArrayList<>());
        }

        return result;
    }

    /**
     * Converts McKinsey 7S to the format expected by comparison service.
     */
    private static Map<String, String> convertMckinsey7s(UserAnalysis.McKinsey7s mckinsey) {
        Map<String, String> result = new HashMap<>();

        if (mckinsey != null) {
            result.put("strategy", mckinsey.getStrategy() != null ? mckinsey.getStrategy() : "");
            result.put("structure", mckinsey.getStructure() != null ? mckinsey.getStructure() : "");
            result.put("systems", mckinsey.getSystems() != null ? mckinsey.getSystems() : "");
            result.put("style", mckinsey.getStyle() != null ? mckinsey.getStyle() : "");
            result.put("staff", mckinsey.getStaff() != null ? mckinsey.getStaff() : "");
            result.put("skills", mckinsey.getSkills() != null ? mckinsey.getSkills() : "");
            result.put("shared_values", mckinsey.getSharedValues() != null ? mckinsey.getSharedValues() : "");
        } else {
            result.put("strategy", "");
            result.put("structure", "");
            result.put("systems", "");
            result.put("style", "");
            result.put("staff", "");
            result.put("skills", "");
            result.put("shared_values", "");
        }

        return result;
    }

    /**
     * Converts BCG Matrix from UserAnalysis format to comparison format.
     * UserAnalysis stores: Map<String, BcgProduct>
     * Comparison service expects: Map<String, Map<String, Double>>
     */
    private static Map<String, Map<String, Double>> convertBcgMatrix(Map<String, UserAnalysis.BcgProduct> bcgMatrix) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        if (bcgMatrix != null) {
            for (Map.Entry<String, UserAnalysis.BcgProduct> entry : bcgMatrix.entrySet()) {
                String productName = entry.getKey();
                UserAnalysis.BcgProduct bcgProduct = entry.getValue();

                if (bcgProduct != null) {
                    Map<String, Double> productData = new HashMap<>();
                    // Use snake_case keys to match the expected format in comparison service
                    productData.put("market_share", bcgProduct.getMarketShare());
                    productData.put("growth_rate", bcgProduct.getGrowthRate());
                    result.put(productName, productData);
                }
            }
        }

        return result;
    }

    /**
     * Validates that a UserAnalysis is suitable for comparison.
     */
    public static boolean isValidForComparison(UserAnalysis analysis) {
        return analysis != null
                && analysis.getStatus() == UserAnalysis.AnalysisStatus.COMPLETED
                && analysis.getCompanyName() != null
                && !analysis.getCompanyName().trim().isEmpty();
    }

    /**
     * Adds metadata to converted analysis indicating it came from an existing
     * analysis.
     */
    public static Map<String, Object> addExistingAnalysisMetadata(Map<String, Object> analysisData, String analysisId) {
        analysisData.put("source", "existing_analysis");
        analysisData.put("analysis_id", analysisId);
        return analysisData;
    }

    /**
     * Adds metadata to analysis indicating it came from new analysis.
     */
    public static Map<String, Object> addNewAnalysisMetadata(Map<String, Object> analysisData) {
        analysisData.put("source", "new_analysis");
        return analysisData;
    }
}