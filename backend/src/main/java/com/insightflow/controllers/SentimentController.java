package com.insightflow.controllers;

import com.insightflow.schedulers.SentimentScheduler;
import com.insightflow.services.SentimentTrendService;
import com.insightflow.services.SentimentTrendVisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class SentimentController {
    
    @Autowired
    private SentimentScheduler sentimentScheduler;
    
    @Autowired
    private SentimentTrendService trendService;
    
    @Autowired
    private SentimentTrendVisualizationService visualizationService;
    
    // Trigger collection for all monitored companies
    @PostMapping("/collect")
    public Map<String, Object> collectSentiment() {
        sentimentScheduler.collectDailySentiment();
        return Map.of(
            "status", "success",
            "message", "Sentiment collection triggered for all monitored companies"
        );
    }
    
    // Trigger collection for specific companies
    @PostMapping("/collect/{companies}")
    public Map<String, Object> collectSentimentForCompanies(@PathVariable String companies) {
        List<String> companyList = Arrays.asList(companies.split(","));
        sentimentScheduler.testWithCompanies(companyList, null);
        return Map.of(
            "status", "success",
            "message", "Sentiment collection triggered for companies: " + companyList
        );
    }
    
    // Get all monitored companies
    @GetMapping("/companies")
    public Map<String, Object> getMonitoredCompanies() {
        return Map.of(
            "status", "success",
            "companies", sentimentScheduler.getMonitoredCompanies()
        );
    }
    
    // Add a company to monitoring
    @PostMapping("/companies/add/{company}")
    public Map<String, Object> addCompany(@PathVariable String company) {
        boolean added = sentimentScheduler.addCompany(company);
        return Map.of(
            "status", added ? "success" : "error",
            "message", added ? "Company added to monitoring" : "Company already monitored or invalid",
            "companies", sentimentScheduler.getMonitoredCompanies()
        );
    }
    
    // Remove a company from monitoring
    @DeleteMapping("/companies/remove/{company}")
    public Map<String, Object> removeCompany(@PathVariable String company) {
        boolean removed = sentimentScheduler.removeCompany(company);
        return Map.of(
            "status", removed ? "success" : "error",
            "message", removed ? "Company removed from monitoring" : "Company not found",
            "companies", sentimentScheduler.getMonitoredCompanies()
        );
    }
    
    // Skip a company in the next run
    @PostMapping("/companies/skip/{company}")
    public Map<String, Object> skipCompanyNextRun(@PathVariable String company) {
        boolean skipped = sentimentScheduler.skipCompanyNextRun(company);
        return Map.of(
            "status", skipped ? "success" : "error",
            "message", skipped ? "Company will be skipped in next run" : "Company not found",
            "companies", sentimentScheduler.getMonitoredCompanies()
        );
    }
    
    // Replace the entire monitoring list
    @PostMapping("/companies/replace")
    public Map<String, Object> replaceCompanies(@RequestBody List<String> companies) {
        sentimentScheduler.setMonitoredCompanies(companies);
        return Map.of(
            "status", "success",
            "message", "Monitoring list updated",
            "companies", sentimentScheduler.getMonitoredCompanies()
        );
    }
    
    // Test with specific companies and sources
    @PostMapping("/test")
    public Map<String, Object> testCollection(
            @RequestParam(required = false) String companies,
            @RequestParam(required = false) String sources) {
        
        List<String> companyList = companies != null ? 
            Arrays.asList(companies.split(",")) : null;
            
        List<String> sourceList = sources != null ? 
            Arrays.asList(sources.split(",")) : null;
            
        sentimentScheduler.testWithCompanies(companyList, sourceList);
        
        return Map.of(
            "status", "success",
            "message", "Test completed for companies: " + 
                      (companyList != null ? companyList : "all") +
                      " with sources: " + 
                      (sourceList != null ? sourceList : "news,social")
        );
    }
    
    // New endpoint for sentiment trend analysis
    @GetMapping("/{companyName}/trend")
    public ResponseEntity<Map<String, Object>> getSentimentTrend(
            @PathVariable String companyName,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String sources,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Sentiment trend requested by " + username + " for " + companyName);

        List<String> sourceList = sources != null ? Arrays.asList(sources.split(",")) : null;

        Map<String, Object> analysis = trendService.analyzeTrends(companyName, days, sourceList);
        analysis.put("requested_by", username);

        return ResponseEntity.ok(analysis);
    }
    
    // Get sentiment trend chart for a company
    @GetMapping("/{companyName}/trend/chart")
    public ResponseEntity<Map<String, Object>> getSentimentTrendChart(
            @PathVariable String companyName,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String sources,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Sentiment trend chart requested by " + username + " for " + companyName);

        List<String> sourceList = sources != null ? Arrays.asList(sources.split(",")) : null;

        try {
            // Get the trend analysis data first
            Map<String, Object> analysis = trendService.analyzeTrends(companyName, days, sourceList);
            
            // Generate chart from the analysis data
            String base64Chart = visualizationService.generateTrendGraph(analysis);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("company", companyName);
            response.put("days", days);
            response.put("sources", sourceList != null ? sourceList : Arrays.asList("news", "social"));
            response.put("chart", base64Chart);
            response.put("analysis", analysis);
            response.put("requested_by", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to generate chart: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Get comparison chart for multiple companies
    @GetMapping("/comparison/chart")
    public ResponseEntity<Map<String, Object>> getComparisonChart(
            @RequestParam String companies,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String sources,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Comparison chart requested by " + username + " for companies: " + companies);

        List<String> companyList = Arrays.asList(companies.split(","));
        List<String> sourceList = sources != null ? Arrays.asList(sources.split(",")) : null;

        try {
            // Get trend analysis data for each company
            Map<String, Map<String, Object>> companiesData = new HashMap<>();
            for (String company : companyList) {
                Map<String, Object> analysis = trendService.analyzeTrends(company, days, sourceList);
                companiesData.put(company, analysis);
            }
            
            // Generate comparison chart from all companies' data
            String base64Chart = visualizationService.generateComparisonChart(companiesData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("companies", companyList);
            response.put("days", days);
            response.put("sources", sourceList != null ? sourceList : Arrays.asList("news", "social"));
            response.put("chart", base64Chart);
            response.put("companies_data", companiesData);
            response.put("requested_by", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to generate comparison chart: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}