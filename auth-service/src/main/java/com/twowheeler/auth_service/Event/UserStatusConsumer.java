package com.twowheeler.auth_service.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.twowheeler.auth_service.Dto.KafkaDTO.UserStatusChangedEvent;
import com.twowheeler.auth_service.Repo.UserCredentialsRepository;
import com.twowheeler.auth_service.Repo.UserStatusRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserStatusConsumer {

    @Autowired
    private UserStatusRepository userStatusRepository;
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @KafkaListener(
        topics = "user-status-events",
        groupId = "auth-service"
    )
    public void consume(UserStatusChangedEvent event) {

        if (event == null || event.userId() == null) {
            return; // safety guard
        }

        boolean statusChanged = event.status() != null;
        boolean roleChanged = event.role() != null;

        /* 🔁 STATUS UPDATE */
        if (statusChanged) {
            userStatusRepository
                .updateStatus(event.userId(), event.status())
                .subscribe();
        }

        /* 🔁 ROLE UPDATE */
        if (roleChanged) {
            userCredentialsRepository
                .updateRole(event.userId(), event.role())
                .subscribe();
        }

        /* 🧠 If both are null → no-op (safe) */
    }
}

