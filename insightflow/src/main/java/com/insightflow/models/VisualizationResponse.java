package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisualizationResponse {
    private String visualizationUrl; // URL to the generated image
    private String description;      // Optional description of the visualization
}