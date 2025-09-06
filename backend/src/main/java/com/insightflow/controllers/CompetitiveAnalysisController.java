package com.insightflow.controllers;

import com.insightflow.services.AnalysisService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
import com.insightflow.services.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class CompetitiveAnalysisController {

    @Autowired
    private RagService ragService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ScrapingService scrapingService;

    @Autowired
    private VisualizationService visualizationService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(
            @RequestPart("company_name") String companyName,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            String filePath = null;
            if (file != null && !file.isEmpty()) {
                Path uploadDir = Paths.get("uploaded_files");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                filePath = uploadDir.resolve(file.getOriginalFilename()).toString();
                Files.write(Paths.get(filePath), file.getBytes());
            }

            Map<String, Object> ragResult = ragService.analyzeCompetitor(filePath, companyName);

            Map<String, List<String>> swot = analysisService.generateSwot(companyName);
            Map<String, List<String>> pestel = analysisService.generatePestel(companyName);
            Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);
            Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);
            Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);

            String swotImage = visualizationService.generateSwotImage(swot);
            String pestelImage = visualizationService.generatePestelImage(pestel);
            String porterImage = visualizationService.generatePorterImage(porter);
            String bcgImage = visualizationService.generateBcgImage(bcg);
            String mckinseyImage = visualizationService.generateMckinseyImage(mckinsey);

            String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);

            Map<String, Object> result = new HashMap<>();
            result.put("company_name", companyName);
            result.put("competitive_analysis", ragResult);
            result.put("swot_lists", swot);
            result.put("pestel_lists", pestel);
            result.put("porter_forces", porter);
            result.put("bcg_matrix", bcg);
            result.put("mckinsey_7s", mckinsey);
            result.put("swot_image", swotImage);
            result.put("pestel_image", pestelImage);
            result.put("porter_image", porterImage);
            result.put("bcg_image", bcgImage);
            result.put("mckinsey_image", mckinseyImage);
            result.put("linkedin_analysis", linkedinAnalysis);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Analysis failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}