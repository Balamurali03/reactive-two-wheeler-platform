package com.twowheeler.user_service.kafka;



import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.twowheeler.user_service.dto.KafkaDTO.UserStatusChangedEvent;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserStatusProducer {

    private final KafkaTemplate<String, UserStatusChangedEvent> kafkaTemplate;

    public void publishStatusChange(String userId, Status status, UserRole role) {
        kafkaTemplate.send(
            "user-status-events",
            userId.toString(),
            new UserStatusChangedEvent(userId, status, role)
        );
    }
}

