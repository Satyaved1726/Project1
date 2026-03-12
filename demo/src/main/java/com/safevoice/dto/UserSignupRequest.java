package com.safevoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserSignupRequest {

    @NotBlank(message = "Name is required")
    @JsonProperty(value = "name", required = false)
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @JsonProperty(value = "email", required = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(value = "password", required = false)
    private String password;

    // Accept frontend field names as well
    @JsonProperty("fullName")
    public void setFullName(String fullName) {
        this.name = fullName;
    }

    @JsonProperty("emailAddress")
    public void setEmailAddress(String emailAddress) {
        this.email = emailAddress;
    }

    public UserSignupRequest() {}

    public UserSignupRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
