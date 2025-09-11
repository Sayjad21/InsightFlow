package com.insightflow.controllers;

import com.insightflow.dto.ComparisonRequest;
import com.insightflow.models.UserAnalysis;
import com.insightflow.services.AnalysisService;
import com.insightflow.services.ComparisonService;
import com.insightflow.services.ComparisonVisualizationService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
import com.insightflow.services.UserService;
import com.insightflow.utils.AnalysisConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/comparison")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ComparisonController {

    @Autowired
    private RagService ragService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ScrapingService scrapingService;

    @Autowired
    private ComparisonVisualizationService visualizationService;

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private UserService userService;

    // Get user's existing analyses for comparison selection
    @GetMapping("/analyses")
    public ResponseEntity<List<Map<String, Object>>> getUserAnalyses(Authentication authentication) {
        String username = authentication.getName();

        try {
            // Get the actual user ID from username/email
            String userId = getUserIdFromUsername(username);
            if (userId == null) {
                return ResponseEntity.badRequest().body(List.of(Map.of("error", "User not found")));
            }

            List<UserAnalysis> analyses = userService.getUserAnalyses(userId);
            List<Map<String, Object>> analysisInfo = new ArrayList<>();

            for (UserAnalysis analysis : analyses) {
                if (analysis.getStatus() == UserAnalysis.AnalysisStatus.COMPLETED) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", analysis.getId());
                    info.put("companyName", analysis.getCompanyName());
                    info.put("analysisDate", analysis.getAnalysisDate());
                    info.put("hasFile", analysis.getUploadedFileName() != null);
                    info.put("fileName", analysis.getUploadedFileName());
                    analysisInfo.add(info);
                }
            }

            return ResponseEntity.ok(analysisInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch analyses: " + e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // Get specific analysis details by ID (useful for preview before comparison)
    @GetMapping("/analyses/{analysisId}")
    public ResponseEntity<Map<String, Object>> getAnalysisById(
            @PathVariable String analysisId,
            Authentication authentication) {
        String username = authentication.getName();

        try {
            // Get the actual user ID from username/email
            String userId = getUserIdFromUsername(username);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Optional<UserAnalysis> analysisOpt = userService.getAnalysisById(analysisId);
            if (analysisOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserAnalysis analysis = analysisOpt.get();

            // Verify the analysis belongs to the current user
            if (!analysis.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            Map<String, Object> result = AnalysisConversionUtil.convertToComparisonFormat(analysis);
            result.put("id", analysis.getId());
            result.put("analysisDate", analysis.getAnalysisDate());
            result.put("status", analysis.getStatus());
            result.put("hasFile", analysis.getUploadedFileName() != null);
            result.put("fileName", analysis.getUploadedFileName());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Compare existing analysis reports
    @PostMapping("/compare-existing")
    public ResponseEntity<Map<String, Object>> compareExistingAnalyses(
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Existing analyses comparison requested by user: " + username);

        // Get the actual user ID from username/email
        String userId = getUserIdFromUsername(username);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        List<String> analysisIds = request.get("analysisIds");
        if (analysisIds == null || analysisIds.size() < 2 || analysisIds.size() > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide 2 to 5 analysis IDs"));
        }

        try {
            List<Map<String, Object>> analyses = new ArrayList<>();

            for (String analysisId : analysisIds) {
                Optional<UserAnalysis> analysisOpt = userService.getAnalysisById(analysisId);
                if (analysisOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Analysis not found: " + analysisId));
                }

                UserAnalysis analysis = analysisOpt.get();

                // Verify the analysis belongs to the current user
                if (!analysis.getUserId().equals(userId)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Unauthorized access to analysis: " + analysisId));
                }

                if (analysis.getStatus() != UserAnalysis.AnalysisStatus.COMPLETED) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Analysis not completed: " + analysisId));
                }

                // Convert UserAnalysis to the format expected by comparison service
                Map<String, Object> analysisData = AnalysisConversionUtil.convertToComparisonFormat(analysis);
                AnalysisConversionUtil.addExistingAnalysisMetadata(analysisData, analysisId);
                analyses.add(analysisData);
            }

            // Perform comparison using existing analysis data
            Map<String, Object> comparisonData = comparisonService.computeComparison(analyses);

            // Generate visualizations
            String radarChart = visualizationService.generateRadarChart(comparisonData);
            String barGraph = visualizationService.generateBarGraph(comparisonData);
            String scatterPlot = visualizationService.generateScatterPlot(comparisonData);

            Map<String, Object> result = new HashMap<>();
            result.put("analyses", analyses);
            result.put("benchmarks", comparisonData.get("benchmarks"));
            result.put("metrics", comparisonData.get("metrics"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("requested_by", username);
            result.put("comparison_type", "existing_analyses");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Comparison failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Compare companies (supports both new analysis and existing analysis IDs)
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareCompanies(
            @RequestParam(value = "company_names", required = false) List<String> companyNames,
            @RequestParam(value = "analysis_ids", required = false) List<String> analysisIds,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Comparison requested by user: " + username);

        // Get the actual user ID from username/email
        String userId = getUserIdFromUsername(username);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Validate input parameters
        int totalItems = (companyNames != null ? companyNames.size() : 0)
                + (analysisIds != null ? analysisIds.size() : 0);
        if (totalItems > 5 || totalItems < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide 2 to 5 companies/analyses to compare"));
        }

        try {
            List<Map<String, Object>> analyses = new ArrayList<>();

            // Process existing analysis IDs first
            if (analysisIds != null && !analysisIds.isEmpty()) {
                for (String analysisId : analysisIds) {
                    Optional<UserAnalysis> analysisOpt = userService.getAnalysisById(analysisId);
                    if (analysisOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Analysis not found: " + analysisId));
                    }

                    UserAnalysis analysis = analysisOpt.get();

                    // Verify the analysis belongs to the current user
                    if (!analysis.getUserId().equals(userId)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Unauthorized access to analysis: " + analysisId));
                    }

                    if (analysis.getStatus() != UserAnalysis.AnalysisStatus.COMPLETED) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Analysis not completed: " + analysisId));
                    }

                    // Convert UserAnalysis to the format expected by comparison service
                    Map<String, Object> analysisData = AnalysisConversionUtil.convertToComparisonFormat(analysis);
                    AnalysisConversionUtil.addExistingAnalysisMetadata(analysisData, analysisId);
                    analyses.add(analysisData);
                }
            }

            // Process new company analyses
            if (companyNames != null && !companyNames.isEmpty()) {
                List<String> filePaths = new ArrayList<>();
                Path uploadDir = Paths.get("uploaded_files");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                if (files != null && !files.isEmpty()) {
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String filePath = uploadDir.resolve(file.getOriginalFilename()).toString();
                            Files.write(Paths.get(filePath), file.getBytes());
                            filePaths.add(filePath);
                        } else {
                            filePaths.add(null);
                        }
                    }
                }

                while (filePaths.size() < companyNames.size()) {
                    filePaths.add(null);
                }

                for (int i = 0; i < companyNames.size(); i++) {
                    String companyName = companyNames.get(i);
                    String filePath = filePaths.get(i);

                    Map<String, Object> ragResult = ragService.analyzeCompetitor(filePath, companyName);

                    Map<String, List<String>> swot = analysisService.generateSwot(companyName);
                    Map<String, List<String>> pestel = analysisService.generatePestel(companyName);
                    Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);
                    Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);
                    Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);

                    String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

                    Map<String, Object> singleResult = new HashMap<>();
                    singleResult.put("company_name", companyName);
                    singleResult.put("summaries", ragResult.get("summaries"));
                    singleResult.put("sources", ragResult.get("sources"));
                    singleResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    singleResult.put("swot_lists", swot);
                    singleResult.put("pestel_lists", pestel);
                    singleResult.put("porter_forces", porter);
                    singleResult.put("bcg_matrix", bcg);
                    singleResult.put("mckinsey_7s", mckinsey);
                    singleResult.put("linkedin_analysis", linkedinAnalysis);

                    analyses.add(singleResult);
                }
            }

            Map<String, Object> comparisonData = comparisonService.computeComparison(analyses);

            String radarChart = visualizationService.generateRadarChart(comparisonData);
            String barGraph = visualizationService.generateBarGraph(comparisonData);
            String scatterPlot = visualizationService.generateScatterPlot(comparisonData);

            Map<String, Object> result = new HashMap<>();
            result.put("analyses", analyses);
            result.put("benchmarks", comparisonData.get("benchmarks"));
            result.put("metrics", comparisonData.get("metrics"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("requested_by", username);
            result.put("comparison_type", "mixed");

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Comparison failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Enhanced comparison endpoint using structured request
    @PostMapping("/compare-enhanced")
    public ResponseEntity<Map<String, Object>> compareEnhanced(
            @RequestBody ComparisonRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Enhanced comparison requested by user: " + username);

        // Get the actual user ID from username/email
        String userId = getUserIdFromUsername(username);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Validate request
        if (!request.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide 2 to 5 companies/analyses to compare"));
        }

        try {
            List<Map<String, Object>> analyses = new ArrayList<>();

            // Process existing analysis IDs
            if (request.hasAnalysisIds()) {
                for (String analysisId : request.getAnalysisIds()) {
                    Optional<UserAnalysis> analysisOpt = userService.getAnalysisById(analysisId);
                    if (analysisOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Analysis not found: " + analysisId));
                    }

                    UserAnalysis analysis = analysisOpt.get();

                    // Verify the analysis belongs to the current user
                    if (!analysis.getUserId().equals(userId)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Unauthorized access to analysis: " + analysisId));
                    }

                    if (analysis.getStatus() != UserAnalysis.AnalysisStatus.COMPLETED) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Analysis not completed: " + analysisId));
                    }

                    Map<String, Object> analysisData = AnalysisConversionUtil.convertToComparisonFormat(analysis);
                    AnalysisConversionUtil.addExistingAnalysisMetadata(analysisData, analysisId);
                    analyses.add(analysisData);
                }
            }

            // Process new company analyses
            if (request.hasCompanyNames()) {
                for (String companyName : request.getCompanyNames()) {
                    Map<String, Object> ragResult = ragService.analyzeCompetitor(null, companyName);

                    Map<String, List<String>> swot = analysisService.generateSwot(companyName);
                    Map<String, List<String>> pestel = analysisService.generatePestel(companyName);
                    Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);
                    Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);
                    Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);

                    String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

                    Map<String, Object> singleResult = new HashMap<>();
                    singleResult.put("company_name", companyName);
                    singleResult.put("summaries", ragResult.get("summaries"));
                    singleResult.put("sources", ragResult.get("sources"));
                    singleResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    singleResult.put("swot_lists", swot);
                    singleResult.put("pestel_lists", pestel);
                    singleResult.put("porter_forces", porter);
                    singleResult.put("bcg_matrix", bcg);
                    singleResult.put("mckinsey_7s", mckinsey);
                    singleResult.put("linkedin_analysis", linkedinAnalysis);
                    AnalysisConversionUtil.addNewAnalysisMetadata(singleResult);

                    analyses.add(singleResult);
                }
            }

            Map<String, Object> comparisonData = comparisonService.computeComparison(analyses);

            String radarChart = visualizationService.generateRadarChart(comparisonData);
            String barGraph = visualizationService.generateBarGraph(comparisonData);
            String scatterPlot = visualizationService.generateScatterPlot(comparisonData);

            Map<String, Object> result = new HashMap<>();
            result.put("analyses", analyses);
            result.put("benchmarks", comparisonData.get("benchmarks"));
            result.put("metrics", comparisonData.get("metrics"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("requested_by", username);
            result.put("comparison_type", request.getComparisonType() != null ? request.getComparisonType() : "mixed");
            result.put("total_items", request.getTotalItemsCount());
            result.put("existing_analyses", request.hasAnalysisIds() ? request.getAnalysisIds().size() : 0);
            result.put("new_analyses", request.hasCompanyNames() ? request.getCompanyNames().size() : 0);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Comparison failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Helper method to get the actual user ID from the username/email stored in JWT
     * token.
     * Since authentication.getName() returns the username (which could be email),
     * we need to
     * look up the actual MongoDB user ID for comparison with UserAnalysis.userId
     */
    private String getUserIdFromUsername(String username) {
        try {
            // First try to find by username
            Optional<com.insightflow.models.User> userOpt = userService.findByUsername(username);

            // If not found, try to find by email (since username might be email)
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(username);
            }

            return userOpt.map(com.insightflow.models.User::getId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error resolving user ID from username: " + username + " - " + e.getMessage());
            return null;
        }
    }
}