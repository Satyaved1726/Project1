package com.safevoice.service;

import com.safevoice.dto.ReportRequest;
import com.safevoice.dto.ReportResponse;
import com.safevoice.dto.AnalyticsResponse;
import com.safevoice.exception.ResourceNotFoundException;
import com.safevoice.model.Report;
import com.safevoice.repository.ReportRepository;
import org.springframework.stereotype.Service;
import com.safevoice.service.SeverityAnalyzerService;
import com.safevoice.service.NotificationService;
import com.safevoice.service.DepartmentRoutingService;
import com.safevoice.service.AuditLogService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final SeverityAnalyzerService severityAnalyzerService;
    private final NotificationService notificationService;
    private final DepartmentRoutingService departmentRoutingService;
    private final AuditLogService auditLogService;

    public ReportService(ReportRepository reportRepository, SeverityAnalyzerService severityAnalyzerService, 
                        NotificationService notificationService, DepartmentRoutingService departmentRoutingService,
                        AuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.severityAnalyzerService = severityAnalyzerService;
        this.notificationService = notificationService;
        this.departmentRoutingService = departmentRoutingService;
        this.auditLogService = auditLogService;
    }

    public ReportResponse submitReport(ReportRequest request) {
        Report report = new Report();
        report.setTitle(request.getTitle());
        report.setCategory(request.getCategory());
        // determine department
        String dept = departmentRoutingService.determineDepartment(request.getCategory());
        report.setAssignedDepartment(dept);
        String detected = severityAnalyzerService.detectSeverity(request.getDescription(), request.getSeverity());
        report.setSeverity(detected);
        report.setDescription(request.getDescription());
        report.setToken(UUID.randomUUID().toString());
        report.setStatus("Pending");
        report.setCreatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        
        // create notification for new report
        notificationService.createNotification(
                "New Anonymous Report Submitted",
                "A new report has been submitted with severity: " + saved.getSeverity(),
                "REPORT_CREATED",
                saved.getId());
        
        return convertToResponse(saved);
    }

    public ReportResponse getReportByToken(String token) {
        Report report = reportRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with token: " + token));
        return convertToResponse(report);
    }

    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ReportResponse updateReportStatus(Long id, String status, String username) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        
        String oldStatus = report.getStatus();
        report.setStatus(status);
        Report updated = reportRepository.save(report);
        
        // Log the action
        auditLogService.logAction(username, "ADMIN", "UPDATE_REPORT_STATUS",
                String.format("Updated report %d status from %s to %s", id, oldStatus, status));
        
        // Create notification for status change
        notificationService.createNotification(
                "Report Status Updated",
                "Report status has been changed to: " + status,
                "REPORT_STATUS_UPDATED",
                id);
        
        return convertToResponse(updated);
    }

    public ReportResponse addAdminResponse(Long id, String response, String username) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        
        report.setAdminResponse(response);
        Report updated = reportRepository.save(report);
        
        // Log the action
        auditLogService.logAction(username, "ADMIN", "ADD_ADMIN_RESPONSE",
                String.format("Added admin response to report %d", id));
        
        // Create notification
        notificationService.createNotification(
                "Admin Response Added",
                "An admin response has been added to your report",
                "ADMIN_RESPONSE_ADDED",
                id);
        
        return convertToResponse(updated);
    }

    public ReportResponse assignDepartment(Long id, String department, String username) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        
        String oldDept = report.getAssignedDepartment();
        report.setAssignedDepartment(department);
        Report updated = reportRepository.save(report);
        
        // Log the action
        auditLogService.logAction(username, "ADMIN", "ASSIGN_DEPARTMENT",
                String.format("Assigned report %d to department: %s (was: %s)", id, department, oldDept));
        
        // Create notification
        notificationService.createNotification(
                "Report Assigned to Department",
                "Report has been assigned to: " + department,
                "DEPARTMENT_ASSIGNED",
                id);
        
        return convertToResponse(updated);
    }

    public long getTotalReports() {
        return reportRepository.count();
    }

    public AnalyticsResponse getAnalytics() {
        long totalReports = getTotalReports();
        
        // Get counts by category
        List<String> categories = reportRepository.findDistinctCategories();
        Map<String, Long> reportsByCategory = new HashMap<>();
        for (String category : categories) {
            reportsByCategory.put(category, reportRepository.countByCategory(category));
        }
        
        // Get counts by severity
        Map<String, Long> reportsBySeverity = new HashMap<>();
        List<String> severities = Arrays.asList("Low", "Medium", "High", "Critical");
        for (String severity : severities) {
            long count = reportRepository.countBySeverity(severity);
            if (count > 0) {
                reportsBySeverity.put(severity, count);
            }
        }
        
        // Get counts by status
        Map<String, Long> reportsByStatus = new HashMap<>();
        List<String> statuses = Arrays.asList("Pending", "In Review", "Resolved");
        for (String status : statuses) {
            reportsByStatus.put(status, reportRepository.countByStatus(status));
        }
        
        return new AnalyticsResponse(totalReports, reportsByCategory, reportsBySeverity, reportsByStatus);
    }

    private ReportResponse convertToResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getToken(),
                report.getTitle(),
                report.getCategory(),
                report.getSeverity(),
                report.getDescription(),
                report.getStatus(),
                report.getAdminResponse(),
                report.getAssignedDepartment(),
                report.getCreatedAt()
        );
    }
}