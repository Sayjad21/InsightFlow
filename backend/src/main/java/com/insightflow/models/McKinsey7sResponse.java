package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class McKinsey7sResponse {
    private String strategy;    // Business strategy
    private String structure;   // Organizational structure
    private String systems;     // Systems and processes
    private String sharedValues;// Shared values/culture
    private String skills;      // Skills of employees
    private String style;       // Leadership style
    private String staff;       // Staff and human resources
    private String visualizationUrl; // Optional: URL to generated 7S diagram/image
}