package com.safevoice.controller;

import com.safevoice.model.AuditLog;
import com.safevoice.service.AuditLogService;
import com.safevoice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Audit Log Controller
 * Handles all endpoints for viewing audit logs
 * Requires ADMIN role
 */
@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Get all audit logs
     * GET /api/audit-logs
     * Requires ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllAuditLogs(Authentication authentication) {
        try {
            List<AuditLog> logs = auditLogService.getAllAuditLogs();
            return ResponseEntity.ok(new ApiResponse(true, "Audit logs retrieved", logs));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve audit logs", null));
        }
    }

    /**
     * Get audit logs by username
     * GET /api/audit-logs/user/{username}
     * Requires ADMIN role
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAuditLogsByUsername(
            @PathVariable String username,
            Authentication authentication) {
        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByUsername(username);
            return ResponseEntity.ok(new ApiResponse(true, "Audit logs retrieved", logs));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve audit logs", null));
        }
    }

    /**
     * Get audit logs by action
     * GET /api/audit-logs/action/{action}
     * Requires ADMIN role
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAuditLogsByAction(
            @PathVariable String action,
            Authentication authentication) {
        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByAction(action);
            return ResponseEntity.ok(new ApiResponse(true, "Audit logs retrieved", logs));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve audit logs", null));
        }
    }

    /**
     * Get audit logs within a time range
     * GET /api/audit-logs/range?start=yyyy-MM-ddThh:mm:ss&end=yyyy-MM-ddThh:mm:ss
     * Requires ADMIN role
     */
    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAuditLogsByTimeRange(
            @RequestParam String start,
            @RequestParam String end,
            Authentication authentication) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startTime = LocalDateTime.parse(start, formatter);
            LocalDateTime endTime = LocalDateTime.parse(end, formatter);
            
            List<AuditLog> logs = auditLogService.getAuditLogsByTimeRange(startTime, endTime);
            return ResponseEntity.ok(new ApiResponse(true, "Audit logs retrieved", logs));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid date format or parameters", null));
        }
    }
}
