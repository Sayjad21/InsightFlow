package com.insightflow.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB entity for storing monitored companies
 */
@Document(collection = "monitored_companies")
public class MonitoredCompany {
    @Id
    private String id;

    @Indexed(unique = true)
    private String companyName;

    private boolean active;
    private LocalDateTime dateAdded;
    private LocalDateTime lastModified;
    private String addedBy; // Future: track who added the company

    // Constructors
    public MonitoredCompany() {
    }

    public MonitoredCompany(String companyName) {
        this.companyName = companyName;
        this.active = true;
        this.dateAdded = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    public MonitoredCompany(String companyName, String addedBy) {
        this(companyName);
        this.addedBy = addedBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        this.lastModified = LocalDateTime.now();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return "MonitoredCompany{" +
                "id='" + id + '\'' +
                ", companyName='" + companyName + '\'' +
                ", active=" + active +
                ", dateAdded=" + dateAdded +
                '}';
    }
}