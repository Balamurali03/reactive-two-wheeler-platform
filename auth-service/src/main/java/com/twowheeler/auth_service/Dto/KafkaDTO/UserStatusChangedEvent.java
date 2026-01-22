package com.twowheeler.auth_service.Dto.KafkaDTO;

import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Enum.Status;

public record UserStatusChangedEvent(
        String userId,
        Status status,
        Roles role
) {
    

}
