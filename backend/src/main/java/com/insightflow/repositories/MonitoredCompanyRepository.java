package com.insightflow.repositories;

import com.insightflow.models.MonitoredCompany;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoredCompanyRepository extends MongoRepository<MonitoredCompany, String> {

    /**
     * Find company by name (case-insensitive)
     */
    Optional<MonitoredCompany> findByCompanyNameIgnoreCase(String companyName);

    /**
     * Check if company exists by name (case-insensitive)
     */
    boolean existsByCompanyNameIgnoreCase(String companyName);

    /**
     * Find all active companies
     */
    List<MonitoredCompany> findByActiveTrue();

    /**
     * Find all inactive companies
     */
    List<MonitoredCompany> findByActiveFalse();

    /**
     * Find companies by who added them
     */
    List<MonitoredCompany> findByAddedBy(String addedBy);

    /**
     * Delete by company name (case-insensitive)
     */
    void deleteByCompanyNameIgnoreCase(String companyName);
}