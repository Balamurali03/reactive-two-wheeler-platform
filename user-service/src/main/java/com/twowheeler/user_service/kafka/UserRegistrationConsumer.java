package com.twowheeler.user_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.twowheeler.user_service.dto.KafkaDTO.UserRegisteredEvent;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.model.UserProfile;
import com.twowheeler.user_service.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegistrationConsumer {

    @Autowired
    private UserProfileRepository userRepository;

    @KafkaListener(
        topics = "user-registration-events",
        groupId = "user-service"
    )
    public void consume(UserRegisteredEvent event) {

        UserProfile user = new UserProfile();
        user.setUserId(event.userId());
        user.setName(event.username());
        user.setStatus(Status.ACTIVE);
        user.setRole(event.role());
        user.setCreatedAt(event.createdAt());
        user.setUpdatedAt(event.createdAt());

        userRepository.save(user).subscribe();
    }
}

