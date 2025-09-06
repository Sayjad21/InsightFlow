package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwotResponse {
    private String strengths;    // Detailed strengths of the business
    private String weaknesses;   // Detailed weaknesses
    private String opportunities; // Detailed opportunities
    private String threats;      // Detailed threats
    private String visualizationUrl; // Optional: URL to generated SWOT chart/image
}