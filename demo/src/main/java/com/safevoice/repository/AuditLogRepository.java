package com.safevoice.repository;

import com.safevoice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    List<AuditLog> findAllOrderByTimestampDesc();
}