package com.safevoice.controller;

import com.safevoice.dto.*;
import com.safevoice.service.ReportService;
import com.safevoice.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Report Controller
 * Handles all admin endpoints for report management
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminReportController {

    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public AdminReportController(ReportService reportService, AuditLogService auditLogService) {
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    /**
     * Get all reports
     * GET /api/admin/reports
     * Requires ADMIN role
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllReports(Authentication authentication) {
        try {
            List<ReportResponse> reports = reportService.getAllReports();
            auditLogService.logAction(authentication.getName(), "ADMIN", "VIEW_ALL_REPORTS",
                    "Fetched all reports");
            return ResponseEntity.ok(new ApiResponse(true, "Reports retrieved", reports));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve reports", null));
        }
    }

    /**
     * Update report status
     * PUT /api/admin/update-status/{id}
     * Requires ADMIN role
     */
    @PutMapping("/update-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        try {
            ReportResponse response = reportService.updateReportStatus(id, request.getStatus(), 
                    authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Report status updated successfully", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * Add admin response to report
     * POST /api/admin/respond/{id}
     * Requires ADMIN role
     */
    @PostMapping("/respond/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addAdminResponse(
            @PathVariable Long id,
            @Valid @RequestBody AdminResponseRequest request,
            Authentication authentication) {
        try {
            ReportResponse response = reportService.addAdminResponse(id, request.getResponse(),
                    authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Admin response added successfully", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * Assign department to report
     * PUT /api/admin/assign-department/{id}
     * Requires ADMIN role
     */
    @PutMapping("/assign-department/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> assignDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentAssignmentRequest request,
            Authentication authentication) {
        try {
            ReportResponse response = reportService.assignDepartment(id, request.getDepartment(),
                    authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Department assigned successfully", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * Get analytics
     * GET /api/admin/analytics
     * Requires ADMIN role
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAnalytics(Authentication authentication) {
        try {
            AnalyticsResponse analytics = reportService.getAnalytics();
            auditLogService.logAction(authentication.getName(), "ADMIN", "VIEW_ANALYTICS",
                    "Viewed analytics");
            return ResponseEntity.ok(new ApiResponse(true, "Analytics retrieved", analytics));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve analytics", null));
        }
    }
}
