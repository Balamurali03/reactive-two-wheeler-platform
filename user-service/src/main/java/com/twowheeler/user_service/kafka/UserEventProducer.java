package com.twowheeler.user_service.kafka;



import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.twowheeler.user_service.dto.KafkaDTO.UserStatusChangedEvent;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.Roles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "auth-events";

    public void publishStatusOrRoleChange(String userId, Status status, Roles role) {

        BaseEvent<UserStatusChangedEvent> event =
        new BaseEvent<>("USER_STATUS_OR_ROLE_CHANGED",
         new UserStatusChangedEvent(userId, status, role)
        );
        log.info("Publishing USER_STATUS_OR_ROLE_CHANGED event for userId={}", userId);
        kafkaTemplate.send(TOPIC, userId, event).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish USER_STATUS_OR_ROLE_CHANGED event", ex);
                    } else {
                        log.info("✅ USER_STATUS_OR_ROLE_CHANGED event published to partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
