package com.insightflow.repositories;

import com.insightflow.models.UserAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAnalysisRepository extends MongoRepository<UserAnalysis, String> {

    // Find all analyses for a specific user
    List<UserAnalysis> findByUserId(String userId);

    // Find analyses by user and status
    List<UserAnalysis> findByUserIdAndStatus(String userId, UserAnalysis.AnalysisStatus status);

    // Find analyses by user ordered by date (most recent first)
    List<UserAnalysis> findByUserIdOrderByAnalysisDateDesc(String userId);

    // Find analyses by company name (case insensitive)
    List<UserAnalysis> findByCompanyNameIgnoreCase(String companyName);

    // Find analyses by date range
    @Query("{ 'analysisDate': { $gte: ?0, $lte: ?1 } }")
    List<UserAnalysis> findByAnalysisDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find analyses by user and date range
    @Query("{ 'userId': ?0, 'analysisDate': { $gte: ?1, $lte: ?2 } }")
    List<UserAnalysis> findByUserIdAndAnalysisDateBetween(String userId, LocalDateTime startDate,
            LocalDateTime endDate);

    // Count total analyses for a user
    long countByUserId(String userId);

    // Count successful analyses for a user
    long countByUserIdAndStatus(String userId, UserAnalysis.AnalysisStatus status);
}
