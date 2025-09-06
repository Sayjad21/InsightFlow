package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkedInRequest {
    private String profileUrl; // Required: LinkedIn profile URL to analyze
}