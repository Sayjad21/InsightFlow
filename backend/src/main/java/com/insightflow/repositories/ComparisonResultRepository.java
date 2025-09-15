package com.insightflow.repositories;

import com.insightflow.models.ComparisonResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComparisonResultRepository extends MongoRepository<ComparisonResult, String> {

    /**
     * Find all comparison results for a specific user
     */
    List<ComparisonResult> findByRequestedByOrderByComparisonDateDesc(String requestedBy);

    /**
     * Find comparison results for a user with pagination
     */
    Page<ComparisonResult> findByRequestedByOrderByComparisonDateDesc(String requestedBy, Pageable pageable);

    /**
     * Find comparison results by user and comparison type
     */
    List<ComparisonResult> findByRequestedByAndComparisonTypeOrderByComparisonDateDesc(
            String requestedBy, String comparisonType);

    /**
     * Find comparison results within a date range for a user
     */
    List<ComparisonResult> findByRequestedByAndComparisonDateBetweenOrderByComparisonDateDesc(
            String requestedBy, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find comparison results that include a specific saved analysis ID
     */
    @Query("{ 'requestedBy': ?0, 'savedAnalysisIds': { $in: [?1] } }")
    List<ComparisonResult> findByRequestedByAndSavedAnalysisIdsContaining(String requestedBy, String analysisId);

    /**
     * Find recent comparison results (last N days) for a user
     */
    @Query("{ 'requestedBy': ?0, 'comparisonDate': { $gte: ?1 } }")
    List<ComparisonResult> findRecentComparisonsByUser(String requestedBy, LocalDateTime fromDate);

    /**
     * Count total comparison results for a user
     */
    long countByRequestedBy(String requestedBy);

    /**
     * Count comparison results by type for a user
     */
    long countByRequestedByAndComparisonType(String requestedBy, String comparisonType);

    /**
     * Delete comparison results older than specified date for a user
     */
    void deleteByRequestedByAndComparisonDateBefore(String requestedBy, LocalDateTime beforeDate);

    /**
     * Find comparison results that contain specific company names in their analyses
     */
    @Query("{ 'requestedBy': ?0, 'analyses.companyName': { $regex: ?1, $options: 'i' } }")
    List<ComparisonResult> findByRequestedByAndCompanyName(String requestedBy, String companyName);
}