package com.twowheeler.auth_service.Event;

import com.twowheeler.auth_service.Dto.KafkaDTO.UserRegisteredEvent;
import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Model.UserCredentials;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final String USER_EVENTS_TOPIC = "user-registration-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserCredentials user) {

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getUserId(),
                user.getUsername(),
                 Roles.USER,
                user.getCreatedAt()
                
        );

        log.info("📤 Publishing USER_REGISTERED event for userId={}", user.getUserId());

        kafkaTemplate.send(USER_EVENTS_TOPIC, user.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish USER_REGISTERED event", ex);
                    } else {
                        log.info("✅ USER_REGISTERED event published to partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
