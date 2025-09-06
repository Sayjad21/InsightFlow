package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RagResponse {
    private String answer; // AI-generated response based on RAG
    private String sources; // Optional: Cited sources from document
    private String visualizationUrl; // Optional: If any chart generated from doc analysis
}