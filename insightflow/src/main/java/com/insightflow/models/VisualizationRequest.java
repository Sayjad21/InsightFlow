package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisualizationRequest {
    private String type; // e.g., "swot", "bcg", "pie", etc.
    private Map<String, Object> data; // Flexible data for chart generation
}