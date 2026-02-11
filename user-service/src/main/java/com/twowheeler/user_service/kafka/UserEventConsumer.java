// package com.twowheeler.user_service.kafka;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Component;

// import com.twowheeler.user_service.dto.KafkaDTO.UserRegisteredEvent;
// import com.twowheeler.user_service.enums.Status;
// import com.twowheeler.user_service.model.UserProfile;
// import com.twowheeler.user_service.repository.UserProfileRepository;

// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class UserRegistrationConsumer {

//     @Autowired
//     private UserProfileRepository userRepository;

//     @KafkaListener(
//         topics = "user-registration-events",
//         groupId = "user-service"
//     )
//     public void consume(UserRegisteredEvent event) {

//         UserProfile user = new UserProfile();
//         user.setUserId(event.userId());
//         user.setName(event.username());
//         user.setStatus(Status.ACTIVE);
//         user.setRole(event.role());
//         user.setCreatedAt(event.createdAt());
//         user.setUpdatedAt(event.createdAt());

//         userRepository.save(user).subscribe();
//     }
// }

package com.twowheeler.user_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twowheeler.user_service.dto.KafkaDTO.UserRegisteredEvent;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.model.UserProfile;
import com.twowheeler.user_service.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final UserProfileRepository userRepository;

    @KafkaListener(topics = "user-events", groupId = "user-service")
    public void consumeUserEvent(BaseEvent<?> event) {

        if ("USER_REGISTERED".equals(event.getEventType())) {
            UserRegisteredEvent payload = objectMapper.convertValue(event.getPayload(), UserRegisteredEvent.class);

            log.info("User service received new user: " + payload.getUsername());

            UserProfile user = new UserProfile();
            user.setUserId(payload.getUserId());
            user.setName(payload.getUsername());
            user.setStatus(Status.ACTIVE);
            user.setRole(payload.getRole());
            user.setCreatedAt(payload.getCreatedAt());
            user.setUpdatedAt(payload.getCreatedAt());

            userRepository.save(user).subscribe();
            log.info("User saved successfully: " + user.getName());
        }
    }
}
