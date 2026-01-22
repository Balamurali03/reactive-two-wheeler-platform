package com.twowheeler.user_service.dto.KafkaDTO;

import java.io.Serializable;
import java.time.Instant;

import com.twowheeler.user_service.enums.UserRole;



public record UserRegisteredEvent(
                String userId,
                String username,
                UserRole role,
                Instant createdAt) implements Serializable {

}
