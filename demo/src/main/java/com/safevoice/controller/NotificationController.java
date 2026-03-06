package com.safevoice.controller;

import com.safevoice.model.Notification;
import com.safevoice.service.NotificationService;
import com.safevoice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse> getUnreadNotifications() {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(new ApiResponse(true, "Unread notifications retrieved", notifications));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve notifications", null));
        }
    }

    /**
     * Get count of unread notifications
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse> getUnreadCount() {
        try {
            long count = notificationService.countUnreadNotifications();
            return ResponseEntity.ok(new ApiResponse(true, "Unread count", count));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to get notification count", null));
        }
    }

    /**
     * Get notifications by report ID
     * GET /api/notifications/report/{reportId}
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse> getNotificationsByReport(@PathVariable Long reportId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByReport(reportId);
            return ResponseEntity.ok(new ApiResponse(true, "Notifications for report retrieved", notifications));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve notifications", null));
        }
    }

    /**
     * Mark notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(new ApiResponse(true, "Notification marked as read", notification));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * Delete notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(new ApiResponse(true, "Notification deleted successfully", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }
}