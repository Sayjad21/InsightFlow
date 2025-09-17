package com.insightflow.controllers;

import com.insightflow.services.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = { 
    "http://localhost:3000", 
    "http://localhost:5173",
    "https://insightflow-frontend-1m77.onrender.com" // ADD THIS LINE
})
public class RagController {

    @Autowired
    private RagService ragService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCompetitor(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        String username = authentication.getName();
        System.out.println("RAG analysis requested by user: " + username);
        
        try {
            String filePath = payload.get("filePath");
            String companyName = payload.get("companyName");

            if (companyName == null || companyName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "companyName is required"));
            }

            Map<String, Object> result = ragService.analyzeCompetitor(filePath, companyName);
            result.put("requested_by", username);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to analyze competitor", "details", e.getMessage()));
        }
    }
}