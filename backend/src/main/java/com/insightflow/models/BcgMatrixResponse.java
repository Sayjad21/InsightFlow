package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BcgMatrixResponse {
    private List<String> stars;         // High growth, high market share products
    private List<String> cashCows;      // Low growth, high market share
    private List<String> questionMarks; // High growth, low market share
    private List<String> dogs;          // Low growth, low market share
    private String analysisSummary;     // Overall summary
    private String visualizationUrl;    // Optional: URL to generated matrix chart/image
}