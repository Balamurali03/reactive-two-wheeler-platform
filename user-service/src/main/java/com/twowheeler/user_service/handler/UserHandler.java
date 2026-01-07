package com.twowheeler.user_service.handler;

import com.twowheeler.user_service.customException.BadRequestException;
import com.twowheeler.user_service.customException.UnauthorizedException;
import com.twowheeler.user_service.customException.UserAlreadyExistsException;
import com.twowheeler.user_service.customException.UserNotFoundException;
import com.twowheeler.user_service.dto.*;
import com.twowheeler.user_service.model.UserProfile;
import com.twowheeler.user_service.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class UserHandler {

    private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

    private final UserProfileRepository repository;

    public UserHandler(UserProfileRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> createProfile(ServerRequest request) {

        log.info("➡️  Create profile request received");

        String userId = request.headers().firstHeader("X-User-Id");
        String role = request.headers().firstHeader("X-User-Role");

        if (userId == null || role == null) {
            log.warn("❌ Missing auth headers while creating profile");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        log.debug("Auth headers validated for userId={}, role={}", userId, role);

        return request.bodyToMono(CreateUserProfileRequest.class)
                .switchIfEmpty(Mono.error(
                        new BadRequestException("Request body is missing")))
                .flatMap(req -> {

                    log.debug("Validating create profile request for userId={}", userId);

                    if (req.name() == null || req.email() == null || req.phone() == null) {
                        log.warn("❌ Invalid create profile request for userId={}", userId);
                        return Mono.error(
                                new BadRequestException("Name, email and phone are required"));
                    }

                    return repository.findByUserId(userId)
                            .flatMap(existing -> {
                                log.warn("❌ User profile already exists for userId={}", userId);
                                return Mono.<UserProfile>error(
                                        new UserAlreadyExistsException(
                                                "User profile already exists"));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.info("✅ Creating new user profile for userId={}", userId);

                                UserProfile profile = new UserProfile();
                                profile.setUserId(userId);
                                profile.setName(req.name());
                                profile.setEmail(req.email());
                                profile.setPhone(req.phone());
                                profile.setRole(role);
                                profile.setCreatedAt(Instant.now().toEpochMilli());
                                profile.setUpdatedAt(Instant.now().toEpochMilli());

                                return repository.save(profile)
                                        .doOnSuccess(p ->
                                                log.info("🎉 User profile created successfully for userId={}", userId));
                            }));
                })
                .flatMap(saved -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new UserProfileResponse(
                                saved.getUserId(),
                                saved.getName(),
                                saved.getEmail(),
                                saved.getPhone(),
                                saved.getRole()
                        )))
                .doOnError(e ->
                        log.error("🔥 Failed to create user profile", e));
    }


    public Mono<ServerResponse> getMe(ServerRequest request) {

        log.info("➡️  Get profile request received");

        String userId = request.headers().firstHeader("X-User-Id");

        if (userId == null) {
            log.warn("❌ Missing auth headers while fetching profile");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        log.debug("Fetching user profile for userId={}", userId);

        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new UserNotFoundException("User profile not found")))
                .flatMap(profile -> {
                    log.info("✅ User profile found for userId={}", userId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new UserProfileResponse(
                                    profile.getUserId(),
                                    profile.getName(),
                                    profile.getEmail(),
                                    profile.getPhone(),
                                    profile.getRole()
                            ));
                })
                .doOnError(e ->
                        log.error("🔥 Failed to fetch user profile for userId={}", userId, e));
    }
}
