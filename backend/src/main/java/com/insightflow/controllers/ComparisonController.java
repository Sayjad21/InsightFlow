package com.insightflow.controllers;

import com.insightflow.dto.ComparisonRequest;
import com.insightflow.models.ComparisonResult;
import com.insightflow.models.UserAnalysis;
import com.insightflow.repositories.ComparisonResultRepository;
import com.insightflow.services.AnalysisService;
import com.insightflow.services.ComparisonService;
import com.insightflow.services.ComparisonVisualizationService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
import com.insightflow.services.UserService;
import com.insightflow.services.VisualizationService;
import com.insightflow.services.TavilyFallbackService;
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
@CrossOrigin(origins = { 
    "http://localhost:3000", 
    "http://localhost:5173",
    "https://insightflow-frontend-1m77.onrender.com" // ADD THIS LINE
})
public class ComparisonController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ComparisonController.class);

    @Autowired
    private RagService ragService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ScrapingService scrapingService;

    @Autowired
    private ComparisonVisualizationService visualizationService;

    @Autowired
    private VisualizationService imageVisualizationService;

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private UserService userService;

    @Autowired
    private ComparisonResultRepository comparisonResultRepository;

    @Autowired
    private TavilyFallbackService tavilyFallbackService;

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

        // Check if user wants to save the comparison result
        Boolean saveResult = (Boolean) request.get("saveResult");
        if (saveResult == null)
            saveResult = false;

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
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("requested_by", username);
            result.put("comparison_type", "existing");
            result.put("saved_analysis_ids", analysisIds); // Set the analysis IDs used

            // Save comparison result if requested
            if (saveResult) {
                try {
                    ComparisonResult comparisonResult = createComparisonResultFromMap(result, username);
                    comparisonResultRepository.save(comparisonResult);
                    result.put("saved", true);
                    result.put("savedId", comparisonResult.getId());
                } catch (Exception e) {
                    // Log error but don't fail the comparison
                    System.err.println("Failed to save comparison result: " + e.getMessage());
                    result.put("saved", false);
                    result.put("saveError", e.getMessage());
                }
            } else {
                result.put("saved", false);
            }

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
            @RequestParam(value = "save_result", required = false, defaultValue = "false") Boolean saveResult,
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

                    // Generate visualization images (stored in Supabase)
                    String swotImage = imageVisualizationService.generateSwotImage(swot);
                    String pestelImage = imageVisualizationService.generatePestelImage(pestel);
                    String porterImage = imageVisualizationService.generatePorterImage(porter);
                    String bcgImage = imageVisualizationService.generateBcgImage(bcg);
                    String mckinseyImage = imageVisualizationService.generateMckinseyImage(mckinsey);

                    String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

                    // Enhanced sources handling with fallback (get from "links" field)
                    @SuppressWarnings("unchecked")
                    List<String> sources = (List<String>) ragResult.get("links");
                    if (sources == null || sources.isEmpty() || sources.size() < 2) {
                        // Generate fallback sources if needed
                        sources = generateAlternativeSources(companyName, sources);
                    }

                    Map<String, Object> tooAddResult = new HashMap<>();
                    tooAddResult.put("company_name", companyName);
                    tooAddResult.put("summaries", ragResult.get("summaries"));
                    tooAddResult.put("sources", sources);
                    tooAddResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    tooAddResult.put("swot_lists", swot);
                    tooAddResult.put("swot_image", swotImage);
                    tooAddResult.put("pestel_lists", pestel);
                    tooAddResult.put("pestel_image", pestelImage);
                    tooAddResult.put("porter_forces", porter);
                    tooAddResult.put("porter_image", porterImage);
                    tooAddResult.put("bcg_matrix", bcg);
                    tooAddResult.put("bcg_image", bcgImage);
                    tooAddResult.put("mckinsey_7s", mckinsey);
                    tooAddResult.put("mckinsey_image", mckinseyImage);
                    tooAddResult.put("linkedin_analysis", linkedinAnalysis);

                    toAddAnalyses.add(tooAddResult);

                    Map<String, Object> singleResult = new HashMap<>();
                    singleResult.put("company_name", companyName);
                    singleResult.put("summaries", ragResult.get("summaries"));
                    singleResult.put("sources", sources);
                    singleResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    singleResult.put("swot_lists", swot);
                    singleResult.put("swot_image", swotImage);
                    singleResult.put("pestel_lists", pestel);
                    singleResult.put("pestel_image", pestelImage);
                    singleResult.put("porter_forces", porter);
                    singleResult.put("porter_image", porterImage);
                    singleResult.put("bcg_matrix", bcg);
                    singleResult.put("bcg_image", bcgImage);
                    singleResult.put("mckinsey_7s", mckinsey);
                    singleResult.put("mckinsey_image", mckinseyImage);
                    singleResult.put("linkedin_analysis", linkedinAnalysis);

                    analyses.add(singleResult);
                }
            }

            Map<String, Object> comparisonData = comparisonService.computeComparison(analyses);

            // Generate visualizations
            String radarChart = visualizationService.generateRadarChart(comparisonData);
            String barGraph = visualizationService.generateBarGraph(comparisonData);
            String scatterPlot = visualizationService.generateScatterPlot(comparisonData);

            // Save new analyses to database if requested
            List<String> savedAnalysisIds = new ArrayList<>();
            if (saveNewAnalyses != null && saveNewAnalyses && toAddAnalyses.size() > 0) {
                savedAnalysisIds = saveNewAnalysesToDatabase(toAddAnalyses, userId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("analyses", analyses);
            result.put("benchmarks", comparisonData.get("benchmarks"));
            result.put("metrics", comparisonData.get("metrics"));
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("requested_by", username);
            result.put("comparison_type", "mixed");
            result.put("saved_analysis_ids", savedAnalysisIds);

            // Save comparison result if requested
            if (saveResult != null && saveResult) {
                try {
                    ComparisonResult comparisonResult = createComparisonResultFromMap(result, username);
                    comparisonResultRepository.save(comparisonResult);
                    result.put("saved", true);
                    result.put("savedId", comparisonResult.getId());
                } catch (Exception e) {
                    // Log error but don't fail the comparison
                    System.err.println("Failed to save comparison result: " + e.getMessage());
                    result.put("saved", false);
                    result.put("saveError", e.getMessage());
                }
            } else {
                result.put("saved", false);
            }

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

                    // Generate visualization images (stored in Supabase)
                    String swotImage = imageVisualizationService.generateSwotImage(swot);
                    String pestelImage = imageVisualizationService.generatePestelImage(pestel);
                    String porterImage = imageVisualizationService.generatePorterImage(porter);
                    String bcgImage = imageVisualizationService.generateBcgImage(bcg);
                    String mckinseyImage = imageVisualizationService.generateMckinseyImage(mckinsey);

                    String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

                    // Enhanced sources handling with fallback (get from "links" field)
                    @SuppressWarnings("unchecked")
                    List<String> sources = (List<String>) ragResult.get("links");
                    if (sources == null || sources.isEmpty() || sources.size() < 2) {
                        // Generate fallback sources if needed
                        sources = generateAlternativeSources(companyName, sources);
                    }

                    Map<String, Object> tooAddResult = new HashMap<>();
                    tooAddResult.put("company_name", companyName);
                    tooAddResult.put("summaries", ragResult.get("summaries"));
                    tooAddResult.put("sources", sources);
                    tooAddResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    tooAddResult.put("swot_lists", swot);
                    tooAddResult.put("swot_image", swotImage);
                    tooAddResult.put("pestel_lists", pestel);
                    tooAddResult.put("pestel_image", pestelImage);
                    tooAddResult.put("porter_forces", porter);
                    tooAddResult.put("porter_image", porterImage);
                    tooAddResult.put("bcg_matrix", bcg);
                    tooAddResult.put("bcg_image", bcgImage);
                    tooAddResult.put("mckinsey_7s", mckinsey);
                    tooAddResult.put("mckinsey_image", mckinseyImage);
                    tooAddResult.put("linkedin_analysis", linkedinAnalysis);

                    toAddAnalyses.add(tooAddResult);

                    Map<String, Object> singleResult = new HashMap<>();
                    singleResult.put("company_name", companyName);
                    singleResult.put("summaries", ragResult.get("summaries"));
                    singleResult.put("sources", sources);
                    singleResult.put("strategy_recommendations", ragResult.get("strategy_recommendations"));
                    singleResult.put("swot_lists", swot);
                    singleResult.put("swot_image", swotImage);
                    singleResult.put("pestel_lists", pestel);
                    singleResult.put("pestel_image", pestelImage);
                    singleResult.put("porter_forces", porter);
                    singleResult.put("porter_image", porterImage);
                    singleResult.put("bcg_matrix", bcg);
                    singleResult.put("bcg_image", bcgImage);
                    singleResult.put("mckinsey_7s", mckinsey);
                    singleResult.put("mckinsey_image", mckinseyImage);
                    singleResult.put("linkedin_analysis", linkedinAnalysis);

                    AnalysisConversionUtil.addNewAnalysisMetadata(singleResult);

                    analyses.add(singleResult);
                }
            }

            Map<String, Object> comparisonData = comparisonService.computeComparison(analyses);

            // Generate visualizations
            String radarChart = visualizationService.generateRadarChart(comparisonData);
            String barGraph = visualizationService.generateBarGraph(comparisonData);
            String scatterPlot = visualizationService.generateScatterPlot(comparisonData);

            // Save new analyses to database if requested
            List<String> savedAnalysisIds = new ArrayList<>();
            if (request.getSaveNewAnalyses() != null && request.getSaveNewAnalyses()) {
                savedAnalysisIds = saveNewAnalysesToDatabase(toAddAnalyses, userId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("analyses", analyses);
            result.put("benchmarks", comparisonData.get("benchmarks"));
            result.put("metrics", comparisonData.get("metrics"));
            result.put("insights", comparisonData.get("insights"));
            result.put("investment_recommendations", comparisonData.get("investment_recommendations"));
            result.put("radar_chart", radarChart);
            result.put("bar_graph", barGraph);
            result.put("scatter_plot", scatterPlot);
            result.put("requested_by", username);
            result.put("comparison_type", request.getComparisonType() != null ? request.getComparisonType() : "mixed");
            result.put("total_items", request.getTotalItemsCount());
            result.put("existing_analyses", request.hasAnalysisIds() ? request.getAnalysisIds().size() : 0);
            result.put("new_analyses", request.hasCompanyNames() ? request.getCompanyNames().size() : 0);
            result.put("saved_analysis_ids", savedAnalysisIds);

            // Save comparison result if requested
            Boolean saveResult = request.getSaveResult();
            if (saveResult != null && saveResult) {
                try {
                    ComparisonResult comparisonResult = createComparisonResultFromMap(result, username);
                    comparisonResultRepository.save(comparisonResult);
                    result.put("saved", true);
                    result.put("savedId", comparisonResult.getId());
                } catch (Exception e) {
                    // Log error but don't fail the comparison
                    System.err.println("Failed to save comparison result: " + e.getMessage());
                    result.put("saved", false);
                    result.put("saveError", e.getMessage());
                }
            } else {
                result.put("saved", false);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Comparison failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test endpoint to verify Tavily fallback service functionality
     */
    @GetMapping("/test-tavily-fallback")
    public ResponseEntity<Map<String, Object>> testTavilyFallback(
            @RequestParam String companyName,
            Authentication authentication) {
        
        String username = authentication.getName();
        logger.info("Testing Tavily fallback for company: {} by user: {}", companyName, username);
        
        long startTime = System.currentTimeMillis();
        
        try {
            String analysis = tavilyFallbackService.getLinkedInAnalysisFallback(companyName);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("company", companyName);
            response.put("analysis", analysis);
            response.put("method", "tavily_fallback");
            response.put("duration_ms", duration);
            response.put("duration_seconds", duration / 1000.0);
            response.put("analysis_length", analysis.length());
            response.put("requested_by", username);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            logger.info("✅ Tavily fallback test successful for {} in {} ms", companyName, duration);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            logger.error("❌ Tavily fallback test failed for {}: {}", companyName, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("company", companyName);
            error.put("error", e.getMessage());
            error.put("error_type", e.getClass().getSimpleName());
            error.put("method", "tavily_fallback");
            error.put("duration_ms", duration);
            error.put("requested_by", username);
            error.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test endpoint to compare Chrome scraping vs Tavily fallback
     */
    @GetMapping("/test-scraping-comparison")
    public ResponseEntity<Map<String, Object>> testScrapingComparison(
            @RequestParam String companyName,
            @RequestParam(defaultValue = "false") boolean forceChrome,
            Authentication authentication) {
        
        String username = authentication.getName();
        logger.info("Testing scraping comparison for company: {} by user: {} (forceChrome: {})", 
                    companyName, username, forceChrome);
        
        Map<String, Object> response = new HashMap<>();
        response.put("company", companyName);
        response.put("requested_by", username);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        // Test Tavily fallback
        logger.info("=== TESTING TAVILY FALLBACK ===");
        long tavilyStart = System.currentTimeMillis();
        try {
            String tavilyAnalysis = tavilyFallbackService.getLinkedInAnalysisFallback(companyName);
            long tavilyEnd = System.currentTimeMillis();
            
            Map<String, Object> tavilyResult = new HashMap<>();
            tavilyResult.put("status", "success");
            tavilyResult.put("analysis", tavilyAnalysis);
            tavilyResult.put("duration_ms", tavilyEnd - tavilyStart);
            tavilyResult.put("analysis_length", tavilyAnalysis.length());
            
            response.put("tavily_fallback", tavilyResult);
            logger.info("✅ Tavily test completed in {} ms", tavilyEnd - tavilyStart);
            
        } catch (Exception e) {
            long tavilyEnd = System.currentTimeMillis();
            
            Map<String, Object> tavilyResult = new HashMap<>();
            tavilyResult.put("status", "error");
            tavilyResult.put("error", e.getMessage());
            tavilyResult.put("error_type", e.getClass().getSimpleName());
            tavilyResult.put("duration_ms", tavilyEnd - tavilyStart);
            
            response.put("tavily_fallback", tavilyResult);
            logger.error("❌ Tavily test failed: {}", e.getMessage());
        }
        
        // Test Chrome scraping (only if forced, to avoid CAPTCHA issues)
        if (forceChrome) {
            logger.info("=== TESTING CHROME SCRAPING ===");
            long chromeStart = System.currentTimeMillis();
            try {
                String chromeAnalysis = scrapingService.getLinkedInAnalysis(companyName);
                long chromeEnd = System.currentTimeMillis();
                
                Map<String, Object> chromeResult = new HashMap<>();
                chromeResult.put("status", "success");
                chromeResult.put("analysis", chromeAnalysis);
                chromeResult.put("duration_ms", chromeEnd - chromeStart);
                chromeResult.put("analysis_length", chromeAnalysis.length());
                
                response.put("chrome_scraping", chromeResult);
                logger.info("✅ Chrome test completed in {} ms", chromeEnd - chromeStart);
                
            } catch (Exception e) {
                long chromeEnd = System.currentTimeMillis();
                
                Map<String, Object> chromeResult = new HashMap<>();
                chromeResult.put("status", "error");
                chromeResult.put("error", e.getMessage());
                chromeResult.put("error_type", e.getClass().getSimpleName());
                chromeResult.put("duration_ms", chromeEnd - chromeStart);
                
                response.put("chrome_scraping", chromeResult);
                logger.error("❌ Chrome test failed: {}", e.getMessage());
            }
        } else {
            Map<String, Object> chromeResult = new HashMap<>();
            chromeResult.put("status", "skipped");
            chromeResult.put("reason", "Chrome scraping skipped to avoid CAPTCHA. Use forceChrome=true to test.");
            response.put("chrome_scraping", chromeResult);
        }
        
        // Add comparison summary
        Map<String, Object> summary = new HashMap<>();
        
        Object tavilyResult = response.get("tavily_fallback");
        if (tavilyResult instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tavily = (Map<String, Object>) tavilyResult;
            summary.put("tavily_success", "success".equals(tavily.get("status")));
            summary.put("tavily_duration", tavily.get("duration_ms"));
        }
        
        Object chromeResult = response.get("chrome_scraping");
        if (chromeResult instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> chrome = (Map<String, Object>) chromeResult;
            summary.put("chrome_success", "success".equals(chrome.get("status")));
            summary.put("chrome_duration", chrome.get("duration_ms"));
            summary.put("chrome_tested", !"skipped".equals(chrome.get("status")));
        }
        
        response.put("summary", summary);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to simulate the fallback chain (Chrome -> Tavily -> Minimal)
     */
    @GetMapping("/test-fallback-chain")
    public ResponseEntity<Map<String, Object>> testFallbackChain(
            @RequestParam String companyName,
            @RequestParam(defaultValue = "true") boolean simulateChromeFailure,
            Authentication authentication) {
        
        String username = authentication.getName();
        logger.info("Testing fallback chain for company: {} by user: {} (simulateFailure: {})", 
                    companyName, username, simulateChromeFailure);
        
        Map<String, Object> response = new HashMap<>();
        response.put("company", companyName);
        response.put("requested_by", username);
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("simulate_chrome_failure", simulateChromeFailure);
        
        List<Map<String, Object>> chainResults = new ArrayList<>();
        
        // Step 1: Chrome scraping (or simulate failure)
        if (simulateChromeFailure) {
            Map<String, Object> chromeStep = new HashMap<>();
            chromeStep.put("step", 1);
            chromeStep.put("method", "chrome_scraping");
            chromeStep.put("status", "simulated_failure");
            chromeStep.put("reason", "Simulated Chrome failure to test fallback chain");
            chromeStep.put("duration_ms", 0);
            chainResults.add(chromeStep);
            
            logger.info("🔄 Step 1: Simulated Chrome failure");
        } else {
            // Actually try Chrome scraping
            Map<String, Object> chromeStep = new HashMap<>();
            chromeStep.put("step", 1);
            chromeStep.put("method", "chrome_scraping");
            
            long chromeStart = System.currentTimeMillis();
            try {
                String chromeAnalysis = scrapingService.getLinkedInAnalysis(companyName);
                long chromeEnd = System.currentTimeMillis();
                
                chromeStep.put("status", "success");
                chromeStep.put("analysis", chromeAnalysis);
                chromeStep.put("duration_ms", chromeEnd - chromeStart);
                chromeStep.put("analysis_length", chromeAnalysis.length());
                
                chainResults.add(chromeStep);
                response.put("final_result", chromeAnalysis);
                response.put("successful_method", "chrome_scraping");
                
                logger.info("✅ Step 1: Chrome scraping successful");
                
            } catch (Exception e) {
                long chromeEnd = System.currentTimeMillis();
                
                chromeStep.put("status", "error");
                chromeStep.put("error", e.getMessage());
                chromeStep.put("error_type", e.getClass().getSimpleName());
                chromeStep.put("duration_ms", chromeEnd - chromeStart);
                
                chainResults.add(chromeStep);
                logger.info("❌ Step 1: Chrome scraping failed: {}", e.getMessage());
            }
        }
        
        // Step 2: Tavily fallback (if Chrome failed or was simulated to fail)
        boolean needsTavilyFallback = simulateChromeFailure || 
                                     chainResults.get(0).get("status").equals("error");
        
        if (needsTavilyFallback) {
            Map<String, Object> tavilyStep = new HashMap<>();
            tavilyStep.put("step", 2);
            tavilyStep.put("method", "tavily_fallback");
            
            long tavilyStart = System.currentTimeMillis();
            try {
                String tavilyAnalysis = tavilyFallbackService.getLinkedInAnalysisFallback(companyName);
                long tavilyEnd = System.currentTimeMillis();
                
                tavilyStep.put("status", "success");
                tavilyStep.put("analysis", tavilyAnalysis);
                tavilyStep.put("duration_ms", tavilyEnd - tavilyStart);
                tavilyStep.put("analysis_length", tavilyAnalysis.length());
                
                chainResults.add(tavilyStep);
                response.put("final_result", tavilyAnalysis);
                response.put("successful_method", "tavily_fallback");
                
                logger.info("✅ Step 2: Tavily fallback successful");
                
            } catch (Exception e) {
                long tavilyEnd = System.currentTimeMillis();
                
                tavilyStep.put("status", "error");
                tavilyStep.put("error", e.getMessage());
                tavilyStep.put("error_type", e.getClass().getSimpleName());
                tavilyStep.put("duration_ms", tavilyEnd - tavilyStart);
                
                chainResults.add(tavilyStep);
                
                // Step 3: Minimal fallback
                Map<String, Object> minimalStep = new HashMap<>();
                minimalStep.put("step", 3);
                minimalStep.put("method", "minimal_fallback");
                minimalStep.put("status", "success");
                minimalStep.put("duration_ms", 0);
                
                String minimalAnalysis = "<strong>Company Analysis: " + companyName + "</strong><br><br>" +
                                       "<em>Analysis could not be completed due to data access limitations.</em><br><br>" +
                                       "<strong>Company:</strong> " + companyName + "<br>" +
                                       "<strong>Status:</strong> All automated methods failed<br>" +
                                       "<strong>Recommendation:</strong> Try again later or use alternative data sources";
                
                minimalStep.put("analysis", minimalAnalysis);
                minimalStep.put("analysis_length", minimalAnalysis.length());
                
                chainResults.add(minimalStep);
                response.put("final_result", minimalAnalysis);
                response.put("successful_method", "minimal_fallback");
                
                logger.info("⚠️ Step 2: Tavily fallback failed: {}", e.getMessage());
                logger.info("🔧 Step 3: Using minimal fallback");
            }
        }
        
        response.put("chain_results", chainResults);
        response.put("total_steps", chainResults.size());
        
        // Calculate total duration
        long totalDuration = chainResults.stream()
                .mapToLong(step -> ((Number) step.getOrDefault("duration_ms", 0)).longValue())
                .sum();
        response.put("total_duration_ms", totalDuration);
        response.put("total_duration_seconds", totalDuration / 1000.0);
        
        return ResponseEntity.ok(response);
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

    // Save comparison result to MongoDB
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveComparisonResult(
            @RequestBody Map<String, Object> comparisonData,
            Authentication authentication) {
        String username = authentication.getName();

        try {
            // Create and populate ComparisonResult
            ComparisonResult comparisonResult = new ComparisonResult();
            comparisonResult.setRequestedBy(username);
            comparisonResult.setComparisonType((String) comparisonData.get("comparison_type"));

            // Set saved analysis IDs
            @SuppressWarnings("unchecked")
            List<String> savedAnalysisIds = (List<String>) comparisonData.get("saved_analysis_ids");
            if (savedAnalysisIds != null) {
                comparisonResult.setSavedAnalysisIds(savedAnalysisIds);
            }

            // Convert analyses data
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> analysesData = (List<Map<String, Object>>) comparisonData.get("analyses");
            if (analysesData != null) {
                List<ComparisonResult.CompanyAnalysis> companyAnalyses = convertToCompanyAnalyses(analysesData);
                comparisonResult.setAnalyses(companyAnalyses);
            }

            // Set metrics
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> metricsData = (List<Map<String, Object>>) comparisonData.get("metrics");
            if (metricsData != null) {
                List<ComparisonResult.ComparisonMetric> metrics = convertToComparisonMetrics(metricsData);
                comparisonResult.setMetrics(metrics);
            }

            // Set benchmarks
            @SuppressWarnings("unchecked")
            Map<String, Object> benchmarksData = (Map<String, Object>) comparisonData.get("benchmarks");
            if (benchmarksData != null) {
                ComparisonResult.ComparisonBenchmarks benchmarks = convertToBenchmarks(benchmarksData);
                comparisonResult.setBenchmarks(benchmarks);
            }

            // Set insights and recommendations
            @SuppressWarnings("unchecked")
            List<String> insights = (List<String>) comparisonData.get("insights");
            if (insights != null) {
                comparisonResult.setInsights(insights);
            }

            comparisonResult.setInvestmentRecommendations((String) comparisonData.get("investment_recommendations"));

            // Set visualization data
            comparisonResult.setRadarChart((String) comparisonData.get("radar_chart"));
            comparisonResult.setBarGraph((String) comparisonData.get("bar_graph"));
            comparisonResult.setScatterPlot((String) comparisonData.get("scatter_plot"));

            // Save to MongoDB
            ComparisonResult savedResult = comparisonResultRepository.save(comparisonResult);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedResult.getId());
            response.put("message", "Comparison result saved successfully");
            response.put("comparisonDate", savedResult.getComparisonDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to save comparison result: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Get saved comparison results for user
    @GetMapping("/saved")
    public ResponseEntity<?> getSavedComparisons(
            @RequestParam(value = "type", required = false) String comparisonType,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Authentication authentication) {
        String username = authentication.getName();

        try {
            List<ComparisonResult> results;

            if (comparisonType != null && !comparisonType.isEmpty()) {
                results = comparisonResultRepository
                        .findByRequestedByAndComparisonTypeOrderByComparisonDateDesc(username, comparisonType);
            } else {
                results = comparisonResultRepository
                        .findByRequestedByOrderByComparisonDateDesc(username);
            }

            // Handle pagination if page parameter is provided
            if (page != null && size != null) {
                int totalElements = results.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalElements);

                List<ComparisonResult> paginatedResults = startIndex < totalElements
                        ? results.subList(startIndex, endIndex)
                        : new ArrayList<>();

                List<Map<String, Object>> content = new ArrayList<>();
                for (ComparisonResult result : paginatedResults) {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", result.getId());
                    summary.put("comparisonDate", result.getComparisonDate());
                    summary.put("comparisonType", result.getComparisonType());
                    summary.put("numberOfCompanies", result.getAnalyses() != null ? result.getAnalyses().size() : 0);

                    // Add company names for easy identification
                    if (result.getAnalyses() != null) {
                        List<String> companyNames = result.getAnalyses().stream()
                                .map(ComparisonResult.CompanyAnalysis::getCompanyName)
                                .toList();
                        summary.put("companyNames", companyNames);
                    }

                    content.add(summary);
                }

                // Return paginated response
                Map<String, Object> paginatedResponse = new HashMap<>();
                paginatedResponse.put("content", content);
                paginatedResponse.put("totalElements", totalElements);
                paginatedResponse.put("totalPages", totalPages);
                paginatedResponse.put("currentPage", page);
                paginatedResponse.put("pageSize", size);
                paginatedResponse.put("hasNext", page < totalPages - 1);
                paginatedResponse.put("hasPrevious", page > 0);

                return ResponseEntity.ok(paginatedResponse);
            }

            // Legacy support: Limit results if specified
            if (limit != null && limit > 0 && results.size() > limit) {
                results = results.subList(0, limit);
            }

            List<Map<String, Object>> response = new ArrayList<>();
            for (ComparisonResult result : results) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", result.getId());
                summary.put("comparisonDate", result.getComparisonDate());
                summary.put("comparisonType", result.getComparisonType());
                summary.put("numberOfCompanies", result.getAnalyses() != null ? result.getAnalyses().size() : 0);

                // Add company names for easy identification
                if (result.getAnalyses() != null) {
                    List<String> companyNames = result.getAnalyses().stream()
                            .map(ComparisonResult.CompanyAnalysis::getCompanyName)
                            .toList();
                    summary.put("companyNames", companyNames);
                }

                response.add(summary);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve saved comparisons: " + e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // Get specific saved comparison result by ID
    @GetMapping("/saved/{comparisonId}")
    public ResponseEntity<ComparisonResult> getSavedComparison(
            @PathVariable String comparisonId,
            Authentication authentication) {
        String username = authentication.getName();

        try {
            Optional<ComparisonResult> resultOpt = comparisonResultRepository.findById(comparisonId);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ComparisonResult result = resultOpt.get();

            // Verify the comparison belongs to the current user
            if (!result.getRequestedBy().equals(username)) {
                return ResponseEntity.status(403).build();
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete saved comparison result
    @DeleteMapping("/saved/{comparisonId}")
    public ResponseEntity<Map<String, Object>> deleteSavedComparison(
            @PathVariable String comparisonId,
            Authentication authentication) {
        String username = authentication.getName();

        try {
            Optional<ComparisonResult> resultOpt = comparisonResultRepository.findById(comparisonId);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ComparisonResult result = resultOpt.get();

            // Verify the comparison belongs to the current user
            if (!result.getRequestedBy().equals(username)) {
                return ResponseEntity.status(403).build();
            }

            comparisonResultRepository.delete(result);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comparison result deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete comparison result: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Helper methods for data conversion
    private List<ComparisonResult.CompanyAnalysis> convertToCompanyAnalyses(List<Map<String, Object>> analysesData) {
        List<ComparisonResult.CompanyAnalysis> companyAnalyses = new ArrayList<>();

        for (Map<String, Object> analysisData : analysesData) {
            ComparisonResult.CompanyAnalysis companyAnalysis = new ComparisonResult.CompanyAnalysis();
            companyAnalysis.setCompanyName((String) analysisData.get("company_name"));
            companyAnalysis.setAnalysisId((String) analysisData.get("analysis_id"));

            // Basic lists
            @SuppressWarnings("unchecked")
            List<String> summaries = (List<String>) analysisData.get("summaries");
            companyAnalysis.setSummaries(summaries);

            @SuppressWarnings("unchecked")
            List<String> competitorAnalysis = (List<String>) analysisData.get("competitor_analysis");
            companyAnalysis.setCompetitorAnalysis(competitorAnalysis);

            @SuppressWarnings("unchecked")
            List<String> sources = (List<String>) analysisData.get("sources");
            companyAnalysis.setSources(sources);

            // Text fields
            companyAnalysis.setStrategyRecommendations((String) analysisData.get("strategy_recommendations"));
            companyAnalysis.setLinkedinAnalysis((String) analysisData.get("linkedin_analysis"));
            companyAnalysis.setRagContext((String) analysisData.get("rag_context"));

            // Images
            companyAnalysis.setSwotImage((String) analysisData.get("swot_image"));
            companyAnalysis.setPestelImage((String) analysisData.get("pestel_image"));
            companyAnalysis.setPorterImage((String) analysisData.get("porter_image"));
            companyAnalysis.setBcgImage((String) analysisData.get("bcg_image"));
            companyAnalysis.setMckinseyImage((String) analysisData.get("mckinsey_image"));

            // Convert SWOT data
            @SuppressWarnings("unchecked")
            Map<String, List<String>> swotData = (Map<String, List<String>>) analysisData.get("swot_lists");
            if (swotData != null) {
                ComparisonResult.SwotLists swot = new ComparisonResult.SwotLists();
                swot.setStrengths(swotData.get("strengths"));
                swot.setWeaknesses(swotData.get("weaknesses"));
                swot.setOpportunities(swotData.get("opportunities"));
                swot.setThreats(swotData.get("threats"));
                companyAnalysis.setSwotLists(swot);
            }

            // Convert PESTEL data
            @SuppressWarnings("unchecked")
            Map<String, List<String>> pestelData = (Map<String, List<String>>) analysisData.get("pestel_lists");
            if (pestelData != null) {
                ComparisonResult.PestelLists pestel = new ComparisonResult.PestelLists();
                pestel.setPolitical(pestelData.get("political"));
                pestel.setEconomic(pestelData.get("economic"));
                pestel.setSocial(pestelData.get("social"));
                pestel.setTechnological(pestelData.get("technological"));
                pestel.setEnvironmental(pestelData.get("environmental"));
                pestel.setLegal(pestelData.get("legal"));
                companyAnalysis.setPestelLists(pestel);
            }

            // Convert Porter Forces data
            @SuppressWarnings("unchecked")
            Map<String, List<String>> porterData = (Map<String, List<String>>) analysisData.get("porter_forces");
            if (porterData != null) {
                ComparisonResult.PorterForces porter = new ComparisonResult.PorterForces();
                porter.setRivalry(porterData.get("rivalry"));
                porter.setNewEntrants(porterData.get("new_entrants"));
                porter.setSubstitutes(porterData.get("substitutes"));
                porter.setBuyerPower(porterData.get("buyer_power"));
                porter.setSupplierPower(porterData.get("supplier_power"));
                companyAnalysis.setPorterForces(porter);
            }

            // Convert BCG Matrix data
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Double>> bcgData = (Map<String, Map<String, Double>>) analysisData
                    .get("bcg_matrix");
            if (bcgData != null) {
                Map<String, ComparisonResult.BcgProduct> bcgMatrix = new HashMap<>();
                for (Map.Entry<String, Map<String, Double>> entry : bcgData.entrySet()) {
                    String productName = entry.getKey();
                    Map<String, Double> productData = entry.getValue();
                    ComparisonResult.BcgProduct product = new ComparisonResult.BcgProduct();
                    if (productData.get("market_share") != null) {
                        product.setMarketShare(productData.get("market_share"));
                    }
                    if (productData.get("growth_rate") != null) {
                        product.setGrowthRate(productData.get("growth_rate"));
                    }
                    bcgMatrix.put(productName, product);
                }
                companyAnalysis.setBcgMatrix(bcgMatrix);
            }

            // Convert McKinsey 7S data
            @SuppressWarnings("unchecked")
            Map<String, String> mckinseyData = (Map<String, String>) analysisData.get("mckinsey_7s");
            if (mckinseyData != null) {
                ComparisonResult.McKinsey7s mckinsey = new ComparisonResult.McKinsey7s();
                mckinsey.setStrategy(mckinseyData.get("strategy"));
                mckinsey.setStructure(mckinseyData.get("structure"));
                mckinsey.setSystems(mckinseyData.get("systems"));
                mckinsey.setStyle(mckinseyData.get("style"));
                mckinsey.setStaff(mckinseyData.get("staff"));
                mckinsey.setSkills(mckinseyData.get("skills"));
                mckinsey.setSharedValues(mckinseyData.get("shared_values"));
                companyAnalysis.setMckinsey7s(mckinsey);
            }

            companyAnalyses.add(companyAnalysis);
        }

        return companyAnalyses;
    }

    private List<ComparisonResult.ComparisonMetric> convertToComparisonMetrics(List<Map<String, Object>> metricsData) {
        List<ComparisonResult.ComparisonMetric> metrics = new ArrayList<>();

        for (Map<String, Object> metricData : metricsData) {
            ComparisonResult.ComparisonMetric metric = new ComparisonResult.ComparisonMetric();
            metric.setSentimentScore(getDoubleValue(metricData.get("sentiment_score")));
            metric.setGrowthRate(getDoubleValue(metricData.get("growth_rate")));
            metric.setRiskRating(getDoubleValue(metricData.get("risk_rating")));
            metric.setMarketShare(getDoubleValue(metricData.get("market_share")));
            metrics.add(metric);
        }

        return metrics;
    }

    private ComparisonResult.ComparisonBenchmarks convertToBenchmarks(Map<String, Object> benchmarksData) {
        ComparisonResult.ComparisonBenchmarks benchmarks = new ComparisonResult.ComparisonBenchmarks();
        benchmarks.setAvgMarketShare(getDoubleValue(benchmarksData.get("avg_market_share")));
        benchmarks.setAvgGrowthRate(getDoubleValue(benchmarksData.get("avg_growth_rate")));
        benchmarks.setAvgRiskRating(getDoubleValue(benchmarksData.get("avg_risk_rating")));
        benchmarks.setAvgSentimentScore(getDoubleValue(benchmarksData.get("avg_sentiment_score")));
        return benchmarks;
    }

    private Double getDoubleValue(Object value) {
        if (value == null)
            return null;
        if (value instanceof Double)
            return (Double) value;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ComparisonResult createComparisonResultFromMap(Map<String, Object> resultMap, String username) {
        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setRequestedBy(username);
        comparisonResult.setComparisonType((String) resultMap.get("comparison_type"));

        @SuppressWarnings("unchecked")
        List<String> savedAnalysisIds = (List<String>) resultMap.get("saved_analysis_ids");
        comparisonResult.setSavedAnalysisIds(savedAnalysisIds);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> analysesData = (List<Map<String, Object>>) resultMap.get("analyses");
        if (analysesData != null) {
            comparisonResult.setAnalyses(convertToCompanyAnalyses(analysesData));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsData = (List<Map<String, Object>>) resultMap.get("metrics");
        if (metricsData != null) {
            comparisonResult.setMetrics(convertToComparisonMetrics(metricsData));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> benchmarksData = (Map<String, Object>) resultMap.get("benchmarks");
        if (benchmarksData != null) {
            comparisonResult.setBenchmarks(convertToBenchmarks(benchmarksData));
        }

        @SuppressWarnings("unchecked")
        List<String> insights = (List<String>) resultMap.get("insights");
        comparisonResult.setInsights(insights);

        comparisonResult.setInvestmentRecommendations((String) resultMap.get("investment_recommendations"));
        comparisonResult.setRadarChart((String) resultMap.get("radar_chart"));
        comparisonResult.setBarGraph((String) resultMap.get("bar_graph"));
        comparisonResult.setScatterPlot((String) resultMap.get("scatter_plot"));

        return comparisonResult;
    }

    /**
     * Generate alternative sources when web scraping fails or returns insufficient
     * sources
     */
    private List<String> generateAlternativeSources(String companyName, List<String> existingSources) {
        List<String> sources = new ArrayList<>();

        // Add existing sources if available
        if (existingSources != null && !existingSources.isEmpty()) {
            sources.addAll(existingSources);
        }

        // If we still have insufficient sources, generate dynamic search-based sources
        if (sources.size() < 3) {
            // Add dynamic search-based source descriptions instead of hardcoded URLs
            sources.add("Company official website and about page");
            sources.add("Professional business networks and company profiles");
            sources.add("Industry databases and business information platforms");
            sources.add("Financial reports and investor relations materials");
            sources.add("Market research and industry analysis reports");
        }

        // Remove duplicates and limit to reasonable number
        return sources.stream()
                .distinct()
                .limit(8)
                .collect(java.util.stream.Collectors.toList());
    }
}