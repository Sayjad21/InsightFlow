package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorAnalysisResponse {
    private Map<String, CompetitorDetails> competitors; // Key: Competitor name, Value: Details
    private String overallSummary; // Summary comparing to the main business
    private String visualizationUrl; // Optional: URL to comparison chart/image

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompetitorDetails {
        private String strengths;
        private String weaknesses;
        private String marketPosition;
    }
}