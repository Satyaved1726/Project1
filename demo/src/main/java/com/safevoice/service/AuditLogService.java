package com.safevoice.service;

import com.safevoice.model.AuditLog;
import com.safevoice.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String username, String role, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setRole(role);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogsByUsername(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllOrderByTimestampDesc();
    }

    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }
}