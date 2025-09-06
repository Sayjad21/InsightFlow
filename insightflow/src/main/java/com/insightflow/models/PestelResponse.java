package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PestelResponse {
    private String political;    // Political factors
    private String economic;     // Economic factors
    private String social;       // Social factors
    private String technological;// Technological factors
    private String environmental;// Environmental factors
    private String legal;        // Legal factors
    private String visualizationUrl; // Optional: URL to generated PESTEL chart/image
}