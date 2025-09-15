package com.insightflow.schedulers;

import com.insightflow.models.SentimentData;
import com.insightflow.repositories.SentimentRepository;
import com.insightflow.services.SentimentFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SentimentScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SentimentScheduler.class);

    @Autowired
    private SentimentFetcherService fetcherService;

    @Autowired
    private SentimentRepository repository;

    // Default companies (thread-safe for dynamic modifications)
    private final List<String> monitoredCompanies = new CopyOnWriteArrayList<>(
        Arrays.asList("Tesla", "Ford", "Apple", "Microsoft", "Amazon", "Google", "Meta", "Netflix")
    );

    // Track companies to skip in the next run
    private final Set<String> skipNextRun = Collections.synchronizedSet(new HashSet<>());

    // @Scheduled(cron = "0 0 9 * * ?")  // Daily at 9 AM
    @Scheduled(cron = "0 */2 * * * ?")  // Every 2 minutes
    public void collectDailySentiment() {
        logger.info("Starting daily sentiment collection for companies: {}", monitoredCompanies);
        
        // Create a copy to avoid ConcurrentModificationException if list changes during iteration
        List<String> companiesToProcess = new ArrayList<>(monitoredCompanies);
        
        // Remove companies that are marked to be skipped
        synchronized(skipNextRun) {
            companiesToProcess.removeAll(skipNextRun);
            skipNextRun.clear(); // Clear skip list after use
        }
        
        for (String company : companiesToProcess) {
            try {
                logger.info("Collecting sentiment data for {}", company);
                List<Map<String, Object>> scoredData = fetcherService.fetchAndScoreSentiment(
                    company, Arrays.asList("news", "social"));

                int savedCount = 0;
                for (Map<String, Object> dataPoint : scoredData) {
                    // Check if exists in last 24h to avoid duplicates
                    LocalDateTime since = LocalDateTime.now().minusHours(24);
                    List<SentimentData> existing = repository.findByCompanyNameAndTimestampBetween(
                        company, since, LocalDateTime.now());
                    
                    // Check if the same sourceIdentifier exists in the last 24 hours
                    String sourceIdentifier = (String) dataPoint.get("sourceIdentifier");
                    boolean alreadyExists = existing.stream()
                        .anyMatch(e -> e.getSourceIdentifier().equals(sourceIdentifier));
                    
                    if (!alreadyExists) {
                        SentimentData entity = new SentimentData(
                            (String) dataPoint.get("companyName"),
                            ((Number) dataPoint.get("sentimentScore")).doubleValue(),  // Changed
                            ((Number) dataPoint.get("riskRating")).doubleValue(),      // Changed
                            (String) dataPoint.get("sourceType"),
                            sourceIdentifier,
                            (LocalDateTime) dataPoint.get("timestamp"),
                            (Map<String, Object>) dataPoint.get("metadata")
                        );
                        repository.save(entity);
                        savedCount++;
                        logger.info("Saved sentiment for {}: {} (Risk: {})", 
                            company, entity.getSentimentScore(), entity.getRiskRating());
                    }
                }
                
                logger.info("Completed processing for {}. Saved {} new records.", company, savedCount);
            } catch (Exception e) {
                logger.error("Error processing sentiment for {}: {}", company, e.getMessage(), e);
            }
        }
        
        logger.info("Completed daily sentiment collection");
    }
    
    // Get all currently monitored companies
    public List<String> getMonitoredCompanies() {
        return new ArrayList<>(monitoredCompanies);
    }
    
    // Add a company to the monitoring list
    public boolean addCompany(String company) {
        if (company != null && !company.trim().isEmpty() && !monitoredCompanies.contains(company)) {
            monitoredCompanies.add(company.trim());
            logger.info("Added company to monitoring: {}", company);
            return true;
        }
        return false;
    }
    
    // Remove a company from the monitoring list
    public boolean removeCompany(String company) {
        boolean removed = monitoredCompanies.remove(company);
        if (removed) {
            logger.info("Removed company from monitoring: {}", company);
        }
        return removed;
    }
    
    // Skip a company in the next run only
    public boolean skipCompanyNextRun(String company) {
        if (monitoredCompanies.contains(company)) {
            skipNextRun.add(company);
            logger.info("Company will be skipped in next run: {}", company);
            return true;
        }
        return false;
    }
    
    // Replace the entire monitoring list
    public void setMonitoredCompanies(List<String> companies) {
        monitoredCompanies.clear();
        if (companies != null) {
            monitoredCompanies.addAll(companies);
        }
        logger.info("Updated monitored companies list: {}", monitoredCompanies);
    }
    
    // Test method with custom companies
    public void testWithCompanies(List<String> companies, List<String> sources) {
        if (companies == null || companies.isEmpty()) {
            companies = monitoredCompanies;
        }
        
        if (sources == null || sources.isEmpty()) {
            sources = Arrays.asList("news", "social");
        }
        
        logger.info("Testing sentiment collection for companies: {} with sources: {}", companies, sources);
        
        for (String company : companies) {
            try {
                logger.info("Testing sentiment data for {}", company);
                List<Map<String, Object>> scoredData = fetcherService.fetchAndScoreSentiment(company, sources);
                logger.info("Retrieved {} data points for {}", scoredData.size(), company);
                
                // Just log, don't save for testing
                for (Map<String, Object> dataPoint : scoredData) {
                    logger.info("Would save: Company={}, Score={}, Risk={}, Source={}", 
                        dataPoint.get("companyName"),
                        dataPoint.get("sentimentScore"),
                        dataPoint.get("riskRating"),
                        dataPoint.get("sourceType"));
                }
            } catch (Exception e) {
                logger.error("Error testing sentiment for {}: {}", company, e.getMessage(), e);
            }
        }
    }
}