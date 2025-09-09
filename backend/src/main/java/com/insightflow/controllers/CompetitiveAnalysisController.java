package com.insightflow.controllers;

import com.insightflow.services.AnalysisService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
import com.insightflow.services.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

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
                @RequestPart(value = "file", required = false) MultipartFile file,
                Authentication authentication) {
            String username = authentication.getName();
            System.out.println("Analysis requested by user: " + username);
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
                result.put("summaries", (List<String>) ragResult.get("summaries"));
                result.put("sources", (List<String>) ragResult.get("links"));
                result.put("strategy_recommendations", (String) ragResult.get("strategy_recommendations"));
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
                result.put("requested_by", username);

                return ResponseEntity.ok(result);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Analysis failed: " + e.getMessage());
                return ResponseEntity.internalServerError().body(error);
            }
        }

    @PostMapping("/generate-company-file")
    public ResponseEntity<Resource> generateCompanyFile(
            @RequestParam("company_name") String companyName,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        System.out.println("File generation requested by user: " + username);
        try {
            // Handle file upload if provided
            String filePath = null;
            if (file != null && !file.isEmpty()) {
                Path uploadDir = Paths.get("uploaded_files");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                filePath = uploadDir.resolve(file.getOriginalFilename()).toString();
                Files.write(Paths.get(filePath), file.getBytes());
            }

            // Generate comprehensive company analysis with differentiation
            String companyAnalysis = generateCompanyAnalysisText(companyName, filePath);

            // Create the file content
            byte[] content = companyAnalysis.getBytes("UTF-8");
            ByteArrayResource resource = new ByteArrayResource(content);

            // Set up response headers for file download
            String filename = companyName.replaceAll("[^a-zA-Z0-9-_]", "_") + "_analysis.txt";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(content.length)
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate company analysis file: " + e.getMessage(), e);
        }
    }

    private String generateCompanyAnalysisText(String companyName, String filePath) {
        StringBuilder analysis = new StringBuilder();

        try {
            // Header
            analysis.append("=".repeat(80)).append("\n");
            analysis.append("COMPREHENSIVE COMPANY ANALYSIS: ").append(companyName.toUpperCase()).append("\n");
            analysis.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n");
            if (filePath != null) {
                analysis.append("Based on uploaded file: ").append(Paths.get(filePath).getFileName()).append("\n");
            }
            analysis.append("=".repeat(80)).append("\n\n");

            // RAG Analysis with Differentiation Strategy
            analysis.append("1. COMPETITIVE INTELLIGENCE & DIFFERENTIATION STRATEGY\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, Object> ragResult = ragService.analyzeCompetitor(filePath, companyName);

                @SuppressWarnings("unchecked")
                List<String> summaries = (List<String>) ragResult.get("summaries");
                String strategyRecommendations = (String) ragResult.get("strategy_recommendations");
                @SuppressWarnings("unchecked")
                List<String> sources = (List<String>) ragResult.get("links");

                if (summaries != null && !summaries.isEmpty()) {
                    analysis.append("MARKET INTELLIGENCE SUMMARY:\n");
                    for (int i = 0; i < summaries.size(); i++) {
                        analysis.append("Summary ").append(i + 1).append(":\n");
                        analysis.append(summaries.get(i)).append("\n\n");
                    }
                }

                if (strategyRecommendations != null && !strategyRecommendations.trim().isEmpty()) {
                    analysis.append("STRATEGIC DIFFERENTIATION RECOMMENDATIONS:\n");
                    // Remove HTML tags for text file
                    String cleanStrategy = strategyRecommendations.replaceAll("<[^>]+>", "")
                            .replaceAll("&nbsp;", " ")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .trim();
                    analysis.append(cleanStrategy).append("\n\n");
                }

                if (sources != null && !sources.isEmpty()) {
                    analysis.append("INFORMATION SOURCES:\n");
                    for (int i = 0; i < sources.size(); i++) {
                        analysis.append(i + 1).append(". ").append(sources.get(i)).append("\n");
                    }
                    analysis.append("\n");
                }

            } catch (Exception e) {
                analysis.append("Competitive analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // LinkedIn Analysis
            analysis.append("2. LINKEDIN INTELLIGENCE ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                String linkedinAnalysis = scrapingService.getLinkedInAnalysis(companyName);
                // Remove HTML tags for text file
                String cleanLinkedIn = linkedinAnalysis.replaceAll("<[^>]+>", "")
                        .replaceAll("&nbsp;", " ")
                        .replaceAll("&amp;", "&")
                        .replaceAll("&lt;", "<")
                        .replaceAll("&gt;", ">")
                        .trim();
                analysis.append(cleanLinkedIn).append("\n\n");
            } catch (Exception e) {
                analysis.append("LinkedIn analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // SWOT Analysis
            analysis.append("3. SWOT ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, List<String>> swot = analysisService.generateSwot(companyName);
                analysis.append("STRENGTHS:\n");
                swot.get("strengths").forEach(s -> analysis.append("• ").append(s).append("\n"));
                analysis.append("\nWEAKNESSES:\n");
                swot.get("weaknesses").forEach(w -> analysis.append("• ").append(w).append("\n"));
                analysis.append("\nOPPORTUNITIES:\n");
                swot.get("opportunities").forEach(o -> analysis.append("• ").append(o).append("\n"));
                analysis.append("\nTHREATS:\n");
                swot.get("threats").forEach(t -> analysis.append("• ").append(t).append("\n"));
                analysis.append("\n");
            } catch (Exception e) {
                analysis.append("SWOT analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // PESTEL Analysis
            analysis.append("4. PESTEL ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, List<String>> pestel = analysisService.generatePestel(companyName);
                analysis.append("POLITICAL:\n");
                pestel.get("political").forEach(p -> analysis.append("• ").append(p).append("\n"));
                analysis.append("\nECONOMIC:\n");
                pestel.get("economic").forEach(e -> analysis.append("• ").append(e).append("\n"));
                analysis.append("\nSOCIAL:\n");
                pestel.get("social").forEach(s -> analysis.append("• ").append(s).append("\n"));
                analysis.append("\nTECHNOLOGICAL:\n");
                pestel.get("technological").forEach(t -> analysis.append("• ").append(t).append("\n"));
                analysis.append("\nENVIRONMENTAL:\n");
                pestel.get("environmental").forEach(env -> analysis.append("• ").append(env).append("\n"));
                analysis.append("\nLEGAL:\n");
                pestel.get("legal").forEach(l -> analysis.append("• ").append(l).append("\n"));
                analysis.append("\n");
            } catch (Exception e) {
                analysis.append("PESTEL analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // Porter's Five Forces
            analysis.append("5. PORTER'S FIVE FORCES ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);
                analysis.append("COMPETITIVE RIVALRY:\n");
                porter.get("rivalry").forEach(r -> analysis.append("• ").append(r).append("\n"));
                analysis.append("\nTHREAT OF NEW ENTRANTS:\n");
                porter.get("new_entrants").forEach(n -> analysis.append("• ").append(n).append("\n"));
                analysis.append("\nTHREAT OF SUBSTITUTES:\n");
                porter.get("substitutes").forEach(s -> analysis.append("• ").append(s).append("\n"));
                analysis.append("\nBUYER POWER:\n");
                porter.get("buyer_power").forEach(b -> analysis.append("• ").append(b).append("\n"));
                analysis.append("\nSUPPLIER POWER:\n");
                porter.get("supplier_power").forEach(s -> analysis.append("• ").append(s).append("\n"));
                analysis.append("\n");
            } catch (Exception e) {
                analysis.append("Porter's Five Forces analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // BCG Matrix
            analysis.append("5. BCG MATRIX ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);
                bcg.forEach((product, metrics) -> {
                    analysis.append("PRODUCT/SERVICE: ").append(product).append("\n");
                    analysis.append("  Market Share: ").append(String.format("%.2f", metrics.get("market_share")))
                            .append("\n");
                    analysis.append("  Growth Rate: ").append(String.format("%.2f%%", metrics.get("growth_rate")))
                            .append("\n");

                    // Categorize based on BCG matrix quadrants
                    double marketShare = metrics.get("market_share");
                    double growthRate = metrics.get("growth_rate");
                    String category;
                    if (marketShare > 0.5 && growthRate > 10) {
                        category = "STAR";
                    } else if (marketShare > 0.5 && growthRate <= 10) {
                        category = "CASH COW";
                    } else if (marketShare <= 0.5 && growthRate > 10) {
                        category = "QUESTION MARK";
                    } else {
                        category = "DOG";
                    }
                    analysis.append("  BCG Category: ").append(category).append("\n\n");
                });
            } catch (Exception e) {
                analysis.append("BCG Matrix analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // McKinsey 7S Model
            analysis.append("6. MCKINSEY 7S MODEL ANALYSIS\n");
            analysis.append("-".repeat(50)).append("\n");
            try {
                Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);
                analysis.append("STRATEGY: ").append(mckinsey.get("strategy")).append("\n\n");
                analysis.append("STRUCTURE: ").append(mckinsey.get("structure")).append("\n\n");
                analysis.append("SYSTEMS: ").append(mckinsey.get("systems")).append("\n\n");
                analysis.append("STYLE: ").append(mckinsey.get("style")).append("\n\n");
                analysis.append("STAFF: ").append(mckinsey.get("staff")).append("\n\n");
                analysis.append("SKILLS: ").append(mckinsey.get("skills")).append("\n\n");
                analysis.append("SHARED VALUES: ").append(mckinsey.get("shared_values")).append("\n\n");
            } catch (Exception e) {
                analysis.append("McKinsey 7S analysis unavailable: ").append(e.getMessage()).append("\n\n");
            }

            // Footer
            analysis.append("=".repeat(80)).append("\n");
            analysis.append("END OF ANALYSIS\n");
            analysis.append("This file can be used for comparative analysis with other companies.\n");
            analysis.append("Upload this file when analyzing competitors for differentiation strategies.\n");
            analysis.append("=".repeat(80)).append("\n");

        } catch (Exception e) {
            analysis.append("ERROR: Failed to generate complete analysis: ").append(e.getMessage()).append("\n");
        }

        return analysis.toString();
    }
}