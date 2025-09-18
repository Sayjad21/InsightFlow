package com.insightflow.controllers;

import com.insightflow.utils.LinkedInSlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for LinkedIn slug generation and validation utilities
 */
@RestController
@RequestMapping("/api/linkedin")
@CrossOrigin(origins = "*")
public class LinkedInSlugController {

    @Autowired
    private LinkedInSlugUtil linkedInSlugUtil;

    /**
     * Generate LinkedIn slug for a company
     */
    @PostMapping("/generate-slug")
    public ResponseEntity<Map<String, Object>> generateSlug(@RequestBody Map<String, String> request) {
        try {
            String companyName = request.get("companyName");

            if (companyName == null || companyName.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Company name is required");
                error.put("usage", "POST /api/linkedin/generate-slug with JSON: {\"companyName\": \"company-name\"}");
                return ResponseEntity.badRequest().body(error);
            }

            long startTime = System.currentTimeMillis();
            String linkedinSlug = linkedInSlugUtil.getLinkedInCompanySlug(companyName.trim());
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("company_name", companyName.trim());
            result.put("linkedin_slug", linkedinSlug);
            result.put("linkedin_url", "https://www.linkedin.com/company/" + linkedinSlug + "/");
            result.put("duration_ms", endTime - startTime);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate LinkedIn slug: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.internalServerError().body(error);
        }
    }
}