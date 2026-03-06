package com.safevoice.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentAssignmentRequest {
    @NotBlank(message = "Department name is required")
    private String department;

    public DepartmentAssignmentRequest() {}

    public DepartmentAssignmentRequest(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
