package com.twowheeler.user_service.dto.KafkaDTO;

import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.Roles;

public class UserStatusChangedEvent {
    private String userId;
    private Status status;
    private Roles role;

    public UserStatusChangedEvent(String userId, Status status, Roles role) {
        this.userId = userId;
        this.status = status;
        this.role = role;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
}


