package com.insightflow.repositories;

import com.insightflow.models.SentimentData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentimentRepository extends MongoRepository<SentimentData, String> {
    List<SentimentData> findByCompanyNameAndTimestampBetween(String companyName, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'companyName': ?0, 'timestamp': { $gte: ?1 } }")
    List<SentimentData> findRecentByCompany(String companyName, LocalDateTime since);
}