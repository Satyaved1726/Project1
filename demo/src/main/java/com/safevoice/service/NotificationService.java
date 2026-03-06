package com.safevoice.service;

import com.safevoice.model.Notification;
import com.safevoice.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(String title, String message, String type, Long reportId) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRelatedReportId(reportId);
        n.setCreatedAt(LocalDateTime.now());
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    public long countUnreadNotifications() {
        return notificationRepository.countByIsReadFalse();
    }

    public List<Notification> getNotificationsByReport(Long reportId) {
        return notificationRepository.findByRelatedReportIdOrderByCreatedAtDesc(reportId);
    }

    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}