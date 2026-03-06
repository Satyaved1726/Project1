package com.safevoice.dto;

import java.util.Map;

public class AnalyticsResponse {
    private long totalReports;
    private Map<String, Long> reportsByCategory;
    private Map<String, Long> reportsBySeverity;
    private Map<String, Long> reportsByStatus;

    public AnalyticsResponse() {}

    public AnalyticsResponse(long totalReports,
                           Map<String, Long> reportsByCategory,
                           Map<String, Long> reportsBySeverity,
                           Map<String, Long> reportsByStatus) {
        this.totalReports = totalReports;
        this.reportsByCategory = reportsByCategory;
        this.reportsBySeverity = reportsBySeverity;
        this.reportsByStatus = reportsByStatus;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public Map<String, Long> getReportsByCategory() {
        return reportsByCategory;
    }

    public void setReportsByCategory(Map<String, Long> reportsByCategory) {
        this.reportsByCategory = reportsByCategory;
    }

    public Map<String, Long> getReportsBySeverity() {
        return reportsBySeverity;
    }

    public void setReportsBySeverity(Map<String, Long> reportsBySeverity) {
        this.reportsBySeverity = reportsBySeverity;
    }

    public Map<String, Long> getReportsByStatus() {
        return reportsByStatus;
    }

    public void setReportsByStatus(Map<String, Long> reportsByStatus) {
        this.reportsByStatus = reportsByStatus;
    }
}
