package com.safevoice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    private String assignedDepartment;

    private LocalDateTime createdAt;

    public Report() {}

    public Report(Long id, String token, String title, String category, String severity,
                  String description, String status, String adminResponse, LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.title = title;
        this.category = category;
        this.severity = severity;
        this.description = description;
        this.status = status;
        this.adminResponse = adminResponse;
        this.createdAt = createdAt;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}