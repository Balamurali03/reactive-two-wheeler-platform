package com.twowheeler.auth_service.Event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.twowheeler.auth_service.Dto.KafkaDTO.UserRegisteredEvent;
import com.twowheeler.auth_service.Enum.Roles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void publishUserRegistered(String userId, String username, Roles role) {
        BaseEvent<UserRegisteredEvent> event =
                new BaseEvent<>("USER_REGISTERED",
                        new UserRegisteredEvent(userId, username,role, java.time.Instant.now()));

                        log.info("Publishing USER_REGISTERED event for userId={}", userId);
        kafkaTemplate.send(TOPIC, userId, event).whenComplete((result, ex) -> {
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

