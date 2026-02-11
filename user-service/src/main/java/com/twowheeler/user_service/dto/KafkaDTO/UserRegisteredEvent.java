package com.twowheeler.user_service.dto.KafkaDTO;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.twowheeler.user_service.enums.Roles;


@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent {
    private String userId;
    private String username;
    private Roles role;
    private Instant createdAt;

    public UserRegisteredEvent() {
    }

    public UserRegisteredEvent(String userId, String username, Roles role, Instant createdAt) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

