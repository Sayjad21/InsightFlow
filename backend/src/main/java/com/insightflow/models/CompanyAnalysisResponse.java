package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyAnalysisResponse {
    private String companyName;
    private List<String> summaries;
    private List<String> competitorAnalysis;
    private String strategyRecommendations;
    private Map<String, List<String>> swotLists;
    private String swotImage;
    private String pestelImage;
    private Map<String, List<String>> porterForces;
    private String porterImage;
    private Map<String, Map<String, Double>> bcgMatrix;
    private String bcgImage;
    private Map<String, String> mckinsey7s;
    private String mckinseyImage;
    private List<String> sources;
    private String linkedinAnalysis;
    private String ragContext;
}
