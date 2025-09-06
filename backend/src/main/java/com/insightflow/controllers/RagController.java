package com.insightflow.controllers;

import com.insightflow.services.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class RagController {

    @Autowired
    private RagService ragService;

    /**
     * Analyze a competitor using optional filePath + companyName.
     *
     * Example API call (POST http://localhost:8000/api/rag/analyze):
     * {
     * "filePath": "uploads/company_report.pdf",
     * "companyName": "Sanofi"
     * }
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCompetitor(@RequestBody Map<String, String> payload) {
        try {
            String filePath = payload.get("filePath"); // optional
            String companyName = payload.get("companyName"); // required

            if (companyName == null || companyName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "companyName is required"));
            }

            Map<String, Object> result = ragService.analyzeCompetitor(filePath, companyName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to analyze competitor", "details", e.getMessage()));
        }
    }
}
