package com.safevoice.service;

import org.springframework.stereotype.Service;

@Service
public class SeverityAnalyzerService {
    public String detectSeverity(String description, String userSeverity) {
        if (description == null) {
            return userSeverity;
        }
        String lower = description.toLowerCase();
        // critical keywords
        if (lower.contains("sexual harassment") || lower.contains("violence") ||
                lower.contains("assault") || lower.contains("threat") || lower.contains("abuse")) {
            return "Critical";
        }
        // high severity keywords
        if (lower.contains("harassment") || lower.contains("bullying") ||
                lower.contains("discrimination") || lower.contains("toxic") || lower.contains("intimidation")) {
            return "High";
        }
        // medium keywords
        if (lower.contains("rude") || lower.contains("unprofessional") ||
                lower.contains("argument") || lower.contains("conflict")) {
            return "Medium";
        }
        return userSeverity;
    }
}