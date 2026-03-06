package com.safevoice.service;

import org.springframework.stereotype.Service;

@Service
public class DepartmentRoutingService {
    public String determineDepartment(String category) {
        if (category == null) {
            return "Admin";
        }
        switch (category) {
            case "Harassment":
            case "Bullying":
            case "Discrimination":
                return "HR";
            case "Workplace Safety":
            case "Ethical Violation":
                return "Compliance";
            default:
                return "Admin";
        }
    }
}