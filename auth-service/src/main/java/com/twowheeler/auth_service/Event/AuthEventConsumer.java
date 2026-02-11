package com.twowheeler.auth_service.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twowheeler.auth_service.Dto.KafkaDTO.UserStatusChangedEvent;
import com.twowheeler.auth_service.Repo.UserCredentialsRepository;
import com.twowheeler.auth_service.Repo.UserStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventConsumer {

    private final ObjectMapper objectMapper;
    private final UserStatusRepository userStatusRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    @KafkaListener(topics = "auth-events", groupId = "auth-service")
    public void consumeUserChanges(BaseEvent<?> event) {

        if ("USER_STATUS_OR_ROLE_CHANGED".equals(event.getEventType())) {
            UserStatusChangedEvent payload = objectMapper.convertValue(event.getPayload(),
                    UserStatusChangedEvent.class);

            if (payload == null || payload.getUserId() == null) {
                return; // safety guard
            }

            boolean statusChanged = payload.getStatus() != null;
            boolean roleChanged = payload.getRole() != null;

            /* 🔁 STATUS UPDATE */
            if (statusChanged) {
                userStatusRepository
                        .updateStatus(payload.getUserId(), payload.getStatus())
                        .subscribe();
                        log.info("User status updated for user ID: {}", payload.getUserId());
            }

            /* 🔁 ROLE UPDATE */
            if (roleChanged) {
                userCredentialsRepository
                        .updateRole(payload.getUserId(), payload.getRole())
                        .subscribe();
                        log.info("User role updated for user ID: {}", payload.getUserId());
            }
        }
    }
}
