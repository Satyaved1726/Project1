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

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public ReportController(ReportService reportService, AuditLogService auditLogService) {
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    /**
     * Submit an anonymous report
     * POST /api/reports/submit
     * Public endpoint - no authentication required
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        try {
            ReportResponse response = reportService.submitReport(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Report submitted successfully", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to submit report: " + ex.getMessage(), null));
        }
    }

    /**
     * Track report by token
     * GET /api/reports/token/{token}
     * Public endpoint - no authentication required
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<ApiResponse> trackReportByToken(@PathVariable String token) {
        try {
            ReportResponse response = reportService.getReportByToken(token);
            return ResponseEntity.ok(new ApiResponse(true, "Report found", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Report not found with token: " + token, null));
        }
    }
}