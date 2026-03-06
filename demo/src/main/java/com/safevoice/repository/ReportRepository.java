package com.safevoice.repository;

import com.safevoice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByToken(String token);
    
    List<Report> findByStatus(String status);
    
    List<Report> findByCategory(String category);
    
    List<Report> findBySeverity(String severity);
    
    long countByStatus(String status);
    
    long countByCategory(String category);
    
    long countBySeverity(String severity);
    
    @Query("SELECT DISTINCT r.category FROM Report r")
    List<String> findDistinctCategories();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = ?1")
    long countReportsByStatus(String status);
}