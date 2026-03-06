package com.safevoice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String type;
    private Long relatedReportId;
    private LocalDateTime createdAt;
    private Boolean isRead = false;

    public Notification() {}

    public Notification(Long id, String title, String message, String type, Long relatedReportId, LocalDateTime createdAt, Boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedReportId = relatedReportId;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getRelatedReportId() { return relatedReportId; }
    public void setRelatedReportId(Long relatedReportId) { this.relatedReportId = relatedReportId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}