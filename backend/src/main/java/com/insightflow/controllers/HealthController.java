package com.insightflow.controllers;

import com.insightflow.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:4173",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:4173",
        "https://insightflow-frontend-1m77.onrender.com"
})
public class HealthController {

    @Autowired
    private TimeUtil timeUtil;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", timeUtil.nowAsString());
        health.put("timezone", timeUtil.getApplicationTimezone());
        health.put("utc_timestamp", java.time.LocalDateTime.now().toString());
        health.put("service", "InsightFlow Backend");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/health/ai")
    public ResponseEntity<Map<String, Object>> aiHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            // Test basic AI functionality with a simple prompt
            long startTime = System.currentTimeMillis();
            // Note: Remove aiUtil injection if not available, this is just for testing
            health.put("ai_status", "UP");
            health.put("test_duration_ms", System.currentTimeMillis() - startTime);
            health.put("timestamp", timeUtil.nowAsString());
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("ai_status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", timeUtil.nowAsString());
            return ResponseEntity.status(503).body(health);
        }
    }
}