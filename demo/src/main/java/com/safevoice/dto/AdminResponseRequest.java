package com.safevoice.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminResponseRequest {
    @NotBlank(message = "Response is required")
    private String response;

    public AdminResponseRequest() {}

    public AdminResponseRequest(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
