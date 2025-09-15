package com.insightflow.services;

import com.insightflow.models.MonitoredCompany;
import com.insightflow.repositories.MonitoredCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonitoredCompanyService {
    private static final Logger logger = LoggerFactory.getLogger(MonitoredCompanyService.class);

    @Autowired
    private MonitoredCompanyRepository repository;

    // Default companies to initialize if database is empty
    private final List<String> DEFAULT_COMPANIES = Arrays.asList(
            "Tesla", "Ford", "Apple", "Microsoft", "Amazon", "Google", "Meta", "Netflix");

    /**
     * Initialize default companies if database is empty
     */
    @PostConstruct
    public void initializeDefaultCompanies() {
        try {
            long count = repository.count();
            if (count == 0) {
                logger.info("Database is empty. Initializing with default companies: {}", DEFAULT_COMPANIES);
                for (String companyName : DEFAULT_COMPANIES) {
                    MonitoredCompany company = new MonitoredCompany(companyName, "system");
                    repository.save(company);
                    logger.info("Added default company: {}", companyName);
                }
                logger.info("Successfully initialized {} default companies", DEFAULT_COMPANIES.size());
            } else {
                logger.info("Found {} monitored companies in database", count);
            }
        } catch (Exception e) {
            logger.error("Error initializing default companies: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all active company names for monitoring
     */
    public List<String> getActiveCompanyNames() {
        try {
            return repository.findByActiveTrue()
                    .stream()
                    .map(MonitoredCompany::getCompanyName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching active companies: {}", e.getMessage(), e);
            return DEFAULT_COMPANIES; // Fallback to defaults
        }
    }

    /**
     * Get all monitored companies (active and inactive)
     */
    public List<MonitoredCompany> getAllMonitoredCompanies() {
        return repository.findAll();
    }

    /**
     * Add a new company to monitoring
     */
    public boolean addCompany(String companyName) {
        return addCompany(companyName, "user");
    }

    /**
     * Add a new company to monitoring with specified addedBy
     */
    public boolean addCompany(String companyName, String addedBy) {
        if (companyName == null || companyName.trim().isEmpty()) {
            logger.warn("Cannot add company with empty name");
            return false;
        }

        String trimmedName = companyName.trim();

        try {
            // Check if company already exists
            if (repository.existsByCompanyNameIgnoreCase(trimmedName)) {
                logger.info("Company already exists: {}", trimmedName);

                // If it exists but is inactive, reactivate it
                Optional<MonitoredCompany> existing = repository.findByCompanyNameIgnoreCase(trimmedName);
                if (existing.isPresent() && !existing.get().isActive()) {
                    MonitoredCompany company = existing.get();
                    company.setActive(true);
                    repository.save(company);
                    logger.info("Reactivated existing company: {}", trimmedName);
                    return true;
                }
                return false;
            }

            // Add new company
            MonitoredCompany newCompany = new MonitoredCompany(trimmedName, addedBy);
            repository.save(newCompany);
            logger.info("Added new company to monitoring: {} (by: {})", trimmedName, addedBy);
            return true;

        } catch (Exception e) {
            logger.error("Error adding company {}: {}", trimmedName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove a company from monitoring (mark as inactive)
     */
    public boolean removeCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<MonitoredCompany> company = repository.findByCompanyNameIgnoreCase(companyName.trim());
            if (company.isPresent()) {
                MonitoredCompany existingCompany = company.get();
                existingCompany.setActive(false);
                repository.save(existingCompany);
                logger.info("Deactivated company: {}", companyName);
                return true;
            } else {
                logger.warn("Company not found for removal: {}", companyName);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error removing company {}: {}", companyName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Permanently delete a company
     */
    public boolean deleteCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return false;
        }

        try {
            repository.deleteByCompanyNameIgnoreCase(companyName.trim());
            logger.info("Permanently deleted company: {}", companyName);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting company {}: {}", companyName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Replace the entire monitoring list
     */
    public void replaceMonitoredCompanies(List<String> companyNames) {
        try {
            // Deactivate all current companies
            List<MonitoredCompany> currentCompanies = repository.findByActiveTrue();
            for (MonitoredCompany company : currentCompanies) {
                company.setActive(false);
                repository.save(company);
            }

            // Add/reactivate the new list
            for (String companyName : companyNames) {
                if (companyName != null && !companyName.trim().isEmpty()) {
                    addCompany(companyName.trim(), "bulk-update");
                }
            }

            logger.info("Replaced monitored companies list with: {}", companyNames);
        } catch (Exception e) {
            logger.error("Error replacing monitored companies: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a company is being monitored
     */
    public boolean isCompanyMonitored(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<MonitoredCompany> company = repository.findByCompanyNameIgnoreCase(companyName.trim());
            return company.isPresent() && company.get().isActive();
        } catch (Exception e) {
            logger.error("Error checking if company is monitored {}: {}", companyName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get company details
     */
    public Optional<MonitoredCompany> getCompanyDetails(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return repository.findByCompanyNameIgnoreCase(companyName.trim());
        } catch (Exception e) {
            logger.error("Error getting company details for {}: {}", companyName, e.getMessage(), e);
            return Optional.empty();
        }
    }
}