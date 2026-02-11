package com.twowheeler.auth_service.Dto.KafkaDTO;

import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Enum.Status;

public class UserStatusChangedEvent {
    private String userId;
    private Status status;
    private Roles role;

    public UserStatusChangedEvent(String userId, Status status, Roles role) {
        this.userId = userId;
        this.status = status;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public Status getStatus() {
        return status;
    }

    public Roles getRole() {
        return role;
    }
}

