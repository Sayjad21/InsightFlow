package com.insightflow.controllers;

import com.insightflow.services.AnalysisService;
import com.insightflow.services.ComparisonService;
import com.insightflow.services.ComparisonVisualizationService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
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

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareCompanies(
            @RequestParam("company_names") List<String> companyNames, // Changed to @RequestParam
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Comparison requested by user: " + username);

        if (companyNames.size() > 5 || companyNames.size() < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide 2 to 5 company names"));
        }

        try {
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

            List<Map<String, Object>> analyses = new ArrayList<>();
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
            result.put("requested_by", username);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Comparison failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}