package com.insightflow.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.utils.AiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalysisService {

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, List<String>> generateSwot(String companyName) {
        String template = aiUtil.getSwotTemplate();
        Map<String, Object> variables = Map.of("company_name", companyName);
        String raw = aiUtil.invokeWithTemplate(template, variables);
        return parseJsonResponse(raw, new TypeReference<Map<String, List<String>>>() {});
    }

    public Map<String, List<String>> generatePestel(String companyName) {
        String template = aiUtil.getPestelTemplate();
        Map<String, Object> variables = Map.of("company_name", companyName);
        String raw = aiUtil.invokeWithTemplate(template, variables);
        return parseJsonResponse(raw, new TypeReference<Map<String, List<String>>>() {});
    }

    public Map<String, List<String>> generatePorterForces(String companyName) {
        String template = aiUtil.getPorterTemplate();
        Map<String, Object> variables = Map.of("company_name", companyName);
        String raw = aiUtil.invokeWithTemplate(template, variables);
        return parseJsonResponse(raw, new TypeReference<Map<String, List<String>>>() {});
    }

    public Map<String, Map<String, Double>> generateBcgMatrix(String companyName) {
        String template = aiUtil.getBcgTemplate();
        Map<String, Object> variables = Map.of("company_name", companyName);
        String raw = aiUtil.invokeWithTemplate(template, variables);
        return parseJsonResponse(raw, new TypeReference<Map<String, Map<String, Double>>>() {});
    }

    public Map<String, String> generateMckinsey7s(String companyName) {
        String template = aiUtil.getMckinseyTemplate();
        Map<String, Object> variables = Map.of("company_name", companyName);
        String raw = aiUtil.invokeWithTemplate(template, variables);
        return parseJsonResponse(raw, new TypeReference<Map<String, String>>() {});
    }

    private <T> T parseJsonResponse(String raw, TypeReference<T> typeReference) {
        try {
            // Attempt direct parsing
            return objectMapper.readValue(raw, typeReference);
        } catch (Exception e) {
            // Fallback: Extract JSON with regex
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(raw);
            if (matcher.find()) {
                String json = matcher.group();
                try {
                    // For BCG Matrix, handle nested object issue
                    if (typeReference.getType().equals(new TypeReference<Map<String, Map<String, Double>>>() {}.getType())) {
                        json = cleanBcgJson(json);
                    }
                    return objectMapper.readValue(json, typeReference);
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid JSON in LLM response: " + raw, ex);
                }
            } else {
                throw new RuntimeException("No JSON found in LLM response: " + raw, e);
            }
        }
    }

    private String cleanBcgJson(String json) {
        try {
            Map<String, Object> rawMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            // If the map has only one key and its value is a Map, flatten it
            if (rawMap.size() == 1) {
                Object inner = rawMap.values().iterator().next();
                if (inner instanceof Map) {
                    rawMap = (Map<String, Object>) inner;
                }
            }
            Map<String, Map<String, Double>> correctedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                String productName = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<String, Object> innerMap = (Map<String, Object>) value;
                    // If the inner map contains the product name as a key, flatten again
                    if (innerMap.containsKey(productName) && innerMap.get(productName) instanceof Map) {
                        innerMap = (Map<String, Object>) innerMap.get(productName);
                    }
                    Map<String, Double> correctedData = new HashMap<>();
                    correctedData.put("market_share", Double.valueOf(innerMap.get("market_share").toString()));
                    correctedData.put("growth_rate", Double.valueOf(innerMap.get("growth_rate").toString()));
                    correctedMap.put(productName, correctedData);
                }
            }
            return objectMapper.writeValueAsString(correctedMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean BCG JSON: " + json, e);
        }
    }
}