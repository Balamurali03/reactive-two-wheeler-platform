package com.twowheeler.user_service.dto.KafkaDTO;

import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.UserRole;

public record UserStatusChangedEvent(
        String userId,
        Status status,
        UserRole role
) {
    

}
