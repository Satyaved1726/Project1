package com.safevoice.repository;

import com.safevoice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    
    List<Notification> findByRelatedReportIdOrderByCreatedAtDesc(Long reportId);
    
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    
    long countByIsReadFalse();
    
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.relatedReportId = ?1")
    void markAsReadByReportId(Long reportId);
}