package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkedInResponse {
    private String summary;      // Profile summary/about section
    private List<String> experience; // List of experience entries
    private List<String> education;  // List of education entries
    private List<String> skills;     // List of skills
    private String analysisInsights; // AI-generated insights
    private String visualizationUrl; // Optional: URL to network/timeline chart
}