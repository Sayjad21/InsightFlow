package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisRequest {
    private String businessName; // Required: Name of the business to analyze
    private String description;  // Required: Brief description of the business
    private List<String> competitors; // Optional: List of competitor names for Competitor Analysis
    private List<String> products;    // Optional: List of products/services for BCG Matrix
}