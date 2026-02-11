package com.twowheeler.auth_service.Event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BaseEvent<T> {

    private String eventId;
    private String eventType;
    private Instant createdAt;
    private T payload;

    public BaseEvent() {
    }

    public BaseEvent(String eventType, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.createdAt = Instant.now();
        this.payload = payload;
    }
}
