package com.twowheeler.auth_service.Dto.KafkaDTO;

import java.io.Serializable;
import java.time.Instant;

import com.twowheeler.auth_service.Enum.Roles;

public record UserRegisteredEvent(
                String userId,
                String username,
                Roles role,
                Instant createdAt) implements Serializable {

}
