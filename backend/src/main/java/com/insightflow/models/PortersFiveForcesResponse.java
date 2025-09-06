package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortersFiveForcesResponse {
    private String threatOfNewEntrants;     // Threat of new entrants
    private String bargainingPowerOfSuppliers; // Bargaining power of suppliers
    private String bargainingPowerOfBuyers;    // Bargaining power of buyers
    private String threatOfSubstituteProducts; // Threat of substitute products/services
    private String rivalryAmongExistingCompetitors; // Rivalry among existing competitors
    private String visualizationUrl; // Optional: URL to generated forces diagram/image
}