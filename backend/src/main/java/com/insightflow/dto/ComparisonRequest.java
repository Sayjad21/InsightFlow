package com.insightflow.dto;

import java.util.List;

public class ComparisonRequest {
    private List<String> companyNames;
    private List<String> analysisIds;
    private String comparisonType; // "new", "existing", or "mixed"
    private Boolean saveNewAnalyses; // Whether to save new analyses to database
    private Boolean saveResult; // Whether to save the comparison result to database

    // Constructors
    public ComparisonRequest() {
    }

    public ComparisonRequest(List<String> companyNames, List<String> analysisIds, String comparisonType) {
        this.companyNames = companyNames;
        this.analysisIds = analysisIds;
        this.comparisonType = comparisonType;
        this.saveNewAnalyses = false; // default to false
    }

    public ComparisonRequest(List<String> companyNames, List<String> analysisIds, String comparisonType,
            Boolean saveNewAnalyses) {
        this.companyNames = companyNames;
        this.analysisIds = analysisIds;
        this.comparisonType = comparisonType;
        this.saveNewAnalyses = saveNewAnalyses;
        this.saveResult = false; // default to false
    }

    // New constructor with saveResult parameter
    public ComparisonRequest(List<String> companyNames, List<String> analysisIds, String comparisonType,
            Boolean saveNewAnalyses, Boolean saveResult) {
        this.companyNames = companyNames;
        this.analysisIds = analysisIds;
        this.comparisonType = comparisonType;
        this.saveNewAnalyses = saveNewAnalyses;
        this.saveResult = saveResult;
    }

    // Getters and Setters
    public List<String> getCompanyNames() {
        return companyNames;
    }

    public void setCompanyNames(List<String> companyNames) {
        this.companyNames = companyNames;
    }

    public List<String> getAnalysisIds() {
        return analysisIds;
    }

    public void setAnalysisIds(List<String> analysisIds) {
        this.analysisIds = analysisIds;
    }

    public String getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(String comparisonType) {
        this.comparisonType = comparisonType;
    }

    public Boolean getSaveNewAnalyses() {
        return saveNewAnalyses;
    }

    public void setSaveNewAnalyses(Boolean saveNewAnalyses) {
        this.saveNewAnalyses = saveNewAnalyses;
    }

    public Boolean getSaveResult() {
        return saveResult;
    }

    public void setSaveResult(Boolean saveResult) {
        this.saveResult = saveResult;
    }

    // Validation methods
    public int getTotalItemsCount() {
        int count = 0;
        if (companyNames != null)
            count += companyNames.size();
        if (analysisIds != null)
            count += analysisIds.size();
        return count;
    }

    public boolean isValid() {
        int total = getTotalItemsCount();
        return total >= 2 && total <= 5;
    }

    public boolean hasCompanyNames() {
        return companyNames != null && !companyNames.isEmpty();
    }

    public boolean hasAnalysisIds() {
        return analysisIds != null && !analysisIds.isEmpty();
    }
}