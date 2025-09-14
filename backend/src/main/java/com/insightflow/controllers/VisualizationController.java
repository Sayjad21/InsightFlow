package com.insightflow.controllers;

import com.insightflow.services.AnalysisService;
import com.insightflow.services.ComparisonVisualizationService;
import com.insightflow.services.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visualizations")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class VisualizationController {

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    private ComparisonVisualizationService comparisonVisualizationService;

    @Autowired
    private AnalysisService analysisService;

    /**
     * Generate SWOT visualization image on demand
     */
    @GetMapping("/swot/{companyName}")
    public ResponseEntity<byte[]> generateSwotImage(@PathVariable String companyName, Authentication authentication) {
        try {
            Map<String, List<String>> swot = analysisService.generateSwot(companyName);
            String base64Image = visualizationService.generateSwotImage(swot);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate PESTEL visualization image on demand
     */
    @GetMapping("/pestel/{companyName}")
    public ResponseEntity<byte[]> generatePestelImage(@PathVariable String companyName, Authentication authentication) {
        try {
            Map<String, List<String>> pestel = analysisService.generatePestel(companyName);
            String base64Image = visualizationService.generatePestelImage(pestel);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate Porter's Five Forces visualization image on demand
     */
    @GetMapping("/porter/{companyName}")
    public ResponseEntity<byte[]> generatePorterImage(@PathVariable String companyName, Authentication authentication) {
        try {
            Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);
            String base64Image = visualizationService.generatePorterImage(porter);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate BCG Matrix visualization image on demand
     */
    @GetMapping("/bcg/{companyName}")
    public ResponseEntity<byte[]> generateBcgImage(@PathVariable String companyName, Authentication authentication) {
        try {
            Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);
            String base64Image = visualizationService.generateBcgImage(bcg);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate McKinsey 7S visualization image on demand
     */
    @GetMapping("/mckinsey/{companyName}")
    public ResponseEntity<byte[]> generateMckinseyImage(@PathVariable String companyName,
            Authentication authentication) {
        try {
            Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);
            String base64Image = visualizationService.generateMckinseyImage(mckinsey);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate comparison radar chart on demand
     */
    @PostMapping("/comparison/radar")
    public ResponseEntity<byte[]> generateComparisonRadarChart(
            @RequestBody Map<String, Object> comparisonData,
            Authentication authentication) {
        try {
            String base64Image = comparisonVisualizationService.generateRadarChart(comparisonData);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate comparison bar chart on demand
     */
    @PostMapping("/comparison/bar")
    public ResponseEntity<byte[]> generateComparisonBarChart(
            @RequestBody Map<String, Object> comparisonData,
            Authentication authentication) {
        try {
            String base64Image = comparisonVisualizationService.generateBarGraph(comparisonData);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate comparison scatter plot on demand
     */
    @PostMapping("/comparison/scatter")
    public ResponseEntity<byte[]> generateComparisonScatterPlot(
            @RequestBody Map<String, Object> comparisonData,
            Authentication authentication) {
        try {
            String base64Image = comparisonVisualizationService.generateScatterPlot(comparisonData);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}