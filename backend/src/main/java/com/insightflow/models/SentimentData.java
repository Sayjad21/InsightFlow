package com.insightflow.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "sentiment_timeseries")
@TimeSeries(timeField = "timestamp", metaField = "metadata", granularity = Granularity.HOURS)
public class SentimentData {
    @Id
    private String id;
    private String companyName;
    private Double sentimentScore; // 0-100 scale
    private Double riskRating; // 0-10 scale (new field)
    private String sourceType; // e.g., "news", "social", "financial"
    private String sourceIdentifier; // e.g., article URL or tweet ID
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    // Constructors
    public SentimentData() {}

    public SentimentData(String companyName, Double sentimentScore, Double riskRating, String sourceType, 
                        String sourceIdentifier, LocalDateTime timestamp, Map<String, Object> metadata) {
        this.companyName = companyName;
        this.sentimentScore = sentimentScore;
        this.riskRating = riskRating;
        this.sourceType = sourceType;
        this.sourceIdentifier = sourceIdentifier;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

    public Double getRiskRating() { return riskRating; }
    public void setRiskRating(Double riskRating) { this.riskRating = riskRating; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceIdentifier() { return sourceIdentifier; }
    public void setSourceIdentifier(String sourceIdentifier) { this.sourceIdentifier = sourceIdentifier; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}