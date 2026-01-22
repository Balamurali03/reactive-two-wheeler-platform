package com.twowheeler.user_service.handler;

import com.twowheeler.user_service.customException.BadRequestException;
import com.twowheeler.user_service.customException.UnauthorizedException;
import com.twowheeler.user_service.customException.UserNotFoundException;
import com.twowheeler.user_service.dto.*;
import com.twowheeler.user_service.kafka.UserStatusProducer;
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
    private final UserStatusProducer statusProducer;

    public UserHandler(
            UserProfileRepository repository,
            UserStatusProducer statusProducer
    ) {
        this.repository = repository;
        this.statusProducer = statusProducer;
    }

    /* ================= UPDATE PROFILE ================= */

    public Mono<ServerResponse> updateProfile(ServerRequest request) {

        log.info("➡️ Update profile request received");

        String userId = request.headers().firstHeader("X-User-Id");
        String roleHeader = request.headers().firstHeader("X-User-Role");

        if (userId == null || roleHeader == null) {
            log.warn("❌ Missing auth headers while updating profile");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        return request.bodyToMono(CreateUserProfileRequest.class)
                .switchIfEmpty(Mono.error(
                        new BadRequestException("Request body is missing")))
                .flatMap(req ->
                        repository.findByUserId(userId)
                                .switchIfEmpty(Mono.error(
                                        new UserNotFoundException("User profile not found")))
                                .flatMap(existing -> {

                                    boolean roleChanged = false;
                                    boolean statusChanged = false;

                                    /* 🔁 ROLE CHANGE CHECK */
                                    if (req.role() != null
                                            && !existing.getRole().equals(req.role())) {

                                        log.info("🔄 Role change detected for userId={}", userId);
                                        existing.setRole(req.role());
                                        roleChanged = true;
                                    }

                                    /* 🔁 STATUS CHANGE CHECK */
                                    if (req.status() != null
                                            && !existing.getStatus().equals(req.status())) {

                                        log.info("🔄 Status change detected for userId={}", userId);
                                        existing.setStatus(req.status());
                                        statusChanged = true;
                                    }

                                    /* ✏️ PROFILE FIELD UPDATES */
                                    if (req.name() != null) existing.setName(req.name());
                                    if (req.email() != null) existing.setEmail(req.email());
                                    if (req.phone() != null) existing.setPhone(req.phone());
                                    if (req.accountType() != null) existing.setAccountType(req.accountType());
                                    if (req.userCategory() != null) existing.setUserCategory(req.userCategory());

                                    existing.setUpdatedAt(Instant.now());

                                    boolean shouldPublish = roleChanged || statusChanged;

                                    return repository.update(existing)
                                            .doOnSuccess(saved -> {
                                                if (shouldPublish) {
                                                    log.info(
                                                            "📤 Publishing user status update event for userId={}",
                                                            userId);
                                                    statusProducer.publishStatusChange(
                                                            userId,
                                                            req.status(),
                                                            req.role()
                                                    );
                                                }
                                            });
                                })
                )
                .flatMap(saved -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new UserProfileResponse(
                                saved.getUserId(),
                                saved.getName(),
                                saved.getEmail(),
                                saved.getPhone(),
                                saved.getRole(),
                                saved.getAccountType(),
                                saved.getUserCategory()
                        )))
                .doOnError(e ->
                        log.error("🔥 Failed to update user profile", e));
    }

    /* ================= GET ME ================= */

    public Mono<ServerResponse> getMe(ServerRequest request) {

        log.info("➡️ Get profile request received");

        String userId = request.headers().firstHeader("X-User-Id");

        if (userId == null) {
            log.warn("❌ Missing auth headers while fetching profile");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new UserNotFoundException("User profile not found")))
                .flatMap(profile -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new UserProfileResponse(
                                profile.getUserId(),
                                profile.getName(),
                                profile.getEmail(),
                                profile.getPhone(),
                                profile.getRole(),
                                profile.getAccountType(),
                                profile.getUserCategory()
                        )))
                .doOnError(e ->
                        log.error("🔥 Failed to fetch user profile for userId={}", userId, e));
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {

    log.info("➡️  Get all users request received");

    String role = request.headers().firstHeader("X-User-Role");

    if (role == null) {
        return Mono.error(new UnauthorizedException("Missing role header"));
    }

    if (!role.equals("ADMIN") && !role.equals("OPERATOR")) {
        return Mono.error(new UnauthorizedException("Access denied"));
    }

    return repository.findAll()
        .map(user -> new UserProfileResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            user.getAccountType(),
            user.getUserCategory()
        ))
        .collectList()
        .flatMap(list ->
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(list)
        )
        .doOnError(e -> log.error("🔥 Failed to fetch user list", e));
}

}
