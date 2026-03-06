package com.safevoice.controller;

import com.safevoice.repository.ReportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ReportRepository reportRepository;

    public HealthController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "UP");
        resp.put("service", "TRINETRA Backend");
        String dbStatus;
        try {
            reportRepository.count();
            dbStatus = "Connected";
        } catch (Exception e) {
            dbStatus = "Disconnected";
        }
        resp.put("database", dbStatus);
        resp.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(resp);
    }
}