package com.insightflow.controllers;

import com.insightflow.dto.ComparisonRequest;
import com.insightflow.models.UserAnalysis;
import com.insightflow.services.AnalysisService;
import com.insightflow.services.ComparisonService;
import com.insightflow.services.ComparisonVisualizationService;
import com.insightflow.services.VisualizationService;
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
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Existing analyses comparison requested by user: " + username);

        // Get the actual user ID from username/email
        String userId = getUserIdFromUsername(username);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        @SuppressWarnings("unchecked")
        List<String> analysisIds = (List<String>) request.get("analysisIds");
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
            result.put("saved_analysis_ids", new ArrayList<>()); // No new analyses to save

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
            @RequestParam(value = "save_new_analyses", required = false, defaultValue = "false") Boolean saveNewAnalyses,
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

            List<Map<String, Object>> toAddAnalyses = new ArrayList<>();

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

                    VisualizationService visualizationServiceUtil = new VisualizationService();

                    String swotImage = visualizationServiceUtil.generateSwotImage(swot);
                    String pestelImage = visualizationServiceUtil.generatePestelImage(pestel);
                    String porterImage = visualizationServiceUtil.generatePorterImage(porter);
                    String bcgImage = visualizationServiceUtil.generateBcgImage(bcg);
                    String mckinseyImage = visualizationServiceUtil.generateMckinseyImage(mckinsey);

                    String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

                    Map<String, Object> tooAddResult = new HashMap<>();
                    tooAddResult.put("company_name", companyName);
                    tooAddResult.put("summaries", ragResult.get("summaries"));
                    tooAddResult.put("sources", ragResult.get("sources"));
                    tooAddResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    tooAddResult.put("swot_lists", swot);
                    tooAddResult.put("pestel_lists", pestel);
                    tooAddResult.put("porter_forces", porter);
                    tooAddResult.put("bcg_matrix", bcg);
                    tooAddResult.put("mckinsey_7s", mckinsey);
                    tooAddResult.put("linkedin_analysis", linkedinAnalysis);

                    tooAddResult.put("swot_image", swotImage);
                    tooAddResult.put("pestel_image", pestelImage);
                    tooAddResult.put("porter_image", porterImage);
                    tooAddResult.put("bcg_image", bcgImage);
                    tooAddResult.put("mckinsey_image", mckinseyImage);

                    toAddAnalyses.add(tooAddResult);

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

            // Save new analyses to database if requested
            List<String> savedAnalysisIds = new ArrayList<>();
            if (saveNewAnalyses != null && saveNewAnalyses && toAddAnalyses.size() > 0) {
                savedAnalysisIds = saveNewAnalysesToDatabase(toAddAnalyses, userId);
            }

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
            result.put("saved_analysis_ids", savedAnalysisIds);

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

            List<Map<String, Object>> toAddAnalyses = new ArrayList<>();

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

                    VisualizationService visualizationServiceUtil = new VisualizationService();

                    String swotImage = visualizationServiceUtil.generateSwotImage(swot);
                    String pestelImage = visualizationServiceUtil.generatePestelImage(pestel);
                    String porterImage = visualizationServiceUtil.generatePorterImage(porter);
                    String bcgImage = visualizationServiceUtil.generateBcgImage(bcg);
                    String mckinseyImage = visualizationServiceUtil.generateMckinseyImage(mckinsey);

                    Map<String, Object> tooAddResult = new HashMap<>();
                    tooAddResult.put("company_name", companyName);
                    tooAddResult.put("summaries", ragResult.get("summaries"));
                    tooAddResult.put("sources", ragResult.get("sources"));
                    tooAddResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    tooAddResult.put("swot_lists", swot);
                    tooAddResult.put("pestel_lists", pestel);
                    tooAddResult.put("porter_forces", porter);
                    tooAddResult.put("bcg_matrix", bcg);
                    tooAddResult.put("mckinsey_7s", mckinsey);
                    tooAddResult.put("linkedin_analysis", linkedinAnalysis);

                    tooAddResult.put("swot_image", swotImage);
                    tooAddResult.put("pestel_image", pestelImage);
                    tooAddResult.put("porter_image", porterImage);
                    tooAddResult.put("bcg_image", bcgImage);
                    tooAddResult.put("mckinsey_image", mckinseyImage);

                    toAddAnalyses.add(tooAddResult);

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

            // Save new analyses to database if requested
            List<String> savedAnalysisIds = new ArrayList<>();
            if (request.getSaveNewAnalyses() != null && request.getSaveNewAnalyses()) {
                savedAnalysisIds = saveNewAnalysesToDatabase(toAddAnalyses, userId);
            }

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
            result.put("saved_analysis_ids", savedAnalysisIds);

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

    /**
     * Helper method to save new analysis data from comparison to user's account
     */
    private List<String> saveNewAnalysesToDatabase(List<Map<String, Object>> newAnalyses, String userId) {
        List<String> savedAnalysisIds = new ArrayList<>();

        for (Map<String, Object> analysisData : newAnalyses) {
            try {
                // Check if this is a new analysis (not from existing database)
                String source = (String) analysisData.get("source");
                if (!"existing_analysis".equals(source)) {
                    UserAnalysis analysis = convertComparisonDataToUserAnalysis(analysisData, userId);
                    analysis.setStatus(UserAnalysis.AnalysisStatus.COMPLETED);
                    analysis.setAnalysisDate(java.time.LocalDateTime.now());

                    UserAnalysis savedAnalysis = userService.saveAnalysis(analysis);
                    savedAnalysisIds.add(savedAnalysis.getId());

                    System.out.println("Saved new analysis for company: " + analysis.getCompanyName()
                            + " with ID: " + savedAnalysis.getId());
                }
            } catch (Exception e) {
                System.err.println("Failed to save analysis for company: " +
                        analysisData.get("company_name") + " - " + e.getMessage());
            }
        }

        return savedAnalysisIds;
    }

    /**
     * Convert comparison analysis data to UserAnalysis object
     */
    private UserAnalysis convertComparisonDataToUserAnalysis(Map<String, Object> analysisData, String userId) {
        String companyName = (String) analysisData.get("company_name");
        UserAnalysis analysis = new UserAnalysis(userId, companyName);

        // Set basic data
        if (analysisData.get("summaries") instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> summaries = (List<String>) analysisData.get("summaries");
            analysis.setSummaries(summaries);
        }

        if (analysisData.get("sources") instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> sources = (List<String>) analysisData.get("sources");
            analysis.setSources(sources);
        }

        if (analysisData.get("strategy_recommendations") instanceof String) {
            analysis.setStrategyRecommendations((String) analysisData.get("strategy_recommendations"));
        }

        if (analysisData.get("linkedin_analysis") instanceof String) {
            analysis.setLinkedinAnalysis((String) analysisData.get("linkedin_analysis"));
        }

        // Set image data
        if (analysisData.get("swot_image") instanceof String) {
            analysis.setSwotImage((String) analysisData.get("swot_image"));
        }

        if (analysisData.get("pestel_image") instanceof String) {
            analysis.setPestelImage((String) analysisData.get("pestel_image"));
        }

        if (analysisData.get("porter_image") instanceof String) {
            analysis.setPorterImage((String) analysisData.get("porter_image"));
        }

        if (analysisData.get("bcg_image") instanceof String) {
            analysis.setBcgImage((String) analysisData.get("bcg_image"));
        }

        if (analysisData.get("mckinsey_image") instanceof String) {
            analysis.setMckinseyImage((String) analysisData.get("mckinsey_image"));
        }

        // Convert SWOT data
        if (analysisData.get("swot_lists") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> swotData = (Map<String, Object>) analysisData.get("swot_lists");
            UserAnalysis.SwotLists swot = new UserAnalysis.SwotLists();

            if (swotData.get("strengths") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> strengths = (List<String>) swotData.get("strengths");
                swot.setStrengths(strengths);
            }
            if (swotData.get("weaknesses") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> weaknesses = (List<String>) swotData.get("weaknesses");
                swot.setWeaknesses(weaknesses);
            }
            if (swotData.get("opportunities") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> opportunities = (List<String>) swotData.get("opportunities");
                swot.setOpportunities(opportunities);
            }
            if (swotData.get("threats") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> threats = (List<String>) swotData.get("threats");
                swot.setThreats(threats);
            }
            analysis.setSwotLists(swot);
        }

        // Convert PESTEL data
        if (analysisData.get("pestel_lists") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pestelData = (Map<String, Object>) analysisData.get("pestel_lists");
            UserAnalysis.PestelLists pestel = new UserAnalysis.PestelLists();

            if (pestelData.get("political") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> political = (List<String>) pestelData.get("political");
                pestel.setPolitical(political);
            }
            if (pestelData.get("economic") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> economic = (List<String>) pestelData.get("economic");
                pestel.setEconomic(economic);
            }
            if (pestelData.get("social") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> social = (List<String>) pestelData.get("social");
                pestel.setSocial(social);
            }
            if (pestelData.get("technological") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> technological = (List<String>) pestelData.get("technological");
                pestel.setTechnological(technological);
            }
            if (pestelData.get("environmental") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> environmental = (List<String>) pestelData.get("environmental");
                pestel.setEnvironmental(environmental);
            }
            if (pestelData.get("legal") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> legal = (List<String>) pestelData.get("legal");
                pestel.setLegal(legal);
            }
            analysis.setPestelLists(pestel);
        }

        // Convert Porter Forces
        if (analysisData.get("porter_forces") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> porterData = (Map<String, Object>) analysisData.get("porter_forces");
            UserAnalysis.PorterForces porter = new UserAnalysis.PorterForces();

            if (porterData.get("new_entrants") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> newEntrants = (List<String>) porterData.get("new_entrants");
                porter.setNewEntrants(newEntrants);
            }
            if (porterData.get("buyer_power") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> buyerPower = (List<String>) porterData.get("buyer_power");
                porter.setBuyerPower(buyerPower);
            }
            if (porterData.get("substitutes") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> substitutes = (List<String>) porterData.get("substitutes");
                porter.setSubstitutes(substitutes);
            }
            if (porterData.get("supplier_power") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> supplierPower = (List<String>) porterData.get("supplier_power");
                porter.setSupplierPower(supplierPower);
            }
            if (porterData.get("rivalry") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> rivalry = (List<String>) porterData.get("rivalry");
                porter.setRivalry(rivalry);
            }
            analysis.setPorterForces(porter);
        }

        // Convert BCG Matrix
        if (analysisData.get("bcg_matrix") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Double>> bcgData = (Map<String, Map<String, Double>>) analysisData
                    .get("bcg_matrix");
            Map<String, UserAnalysis.BcgProduct> bcgMap = new HashMap<>();

            for (Map.Entry<String, Map<String, Double>> entry : bcgData.entrySet()) {
                UserAnalysis.BcgProduct product = new UserAnalysis.BcgProduct();

                Map<String, Double> metrics = entry.getValue();
                if (metrics != null) {
                    Double marketShare = metrics.get("marketShare");
                    if (marketShare == null)
                        marketShare = metrics.get("market_share");
                    if (marketShare != null)
                        product.setMarketShare(marketShare);

                    Double growthRate = metrics.get("growthRate");
                    if (growthRate == null)
                        growthRate = metrics.get("growth_rate");
                    if (growthRate != null)
                        product.setGrowthRate(growthRate);
                }
                bcgMap.put(entry.getKey(), product);
            }
            analysis.setBcgMatrix(bcgMap);
        }

        // Convert McKinsey 7S
        if (analysisData.get("mckinsey_7s") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, String> mckinseyData = (Map<String, String>) analysisData.get("mckinsey_7s");
            UserAnalysis.McKinsey7s mckinsey = new UserAnalysis.McKinsey7s();

            mckinsey.setStrategy(mckinseyData.get("strategy"));
            mckinsey.setStructure(mckinseyData.get("structure"));
            mckinsey.setSystems(mckinseyData.get("systems"));
            mckinsey.setStyle(mckinseyData.get("style"));
            mckinsey.setStaff(mckinseyData.get("staff"));
            mckinsey.setSkills(mckinseyData.get("skills"));
            mckinsey.setSharedValues(mckinseyData.get("shared_values"));

            analysis.setMckinsey7s(mckinsey);
        }

        return analysis;
    }
}