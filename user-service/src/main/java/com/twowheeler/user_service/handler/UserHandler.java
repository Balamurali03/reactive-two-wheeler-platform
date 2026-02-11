package com.twowheeler.user_service.handler;

import com.twowheeler.user_service.customException.BadRequestException;
import com.twowheeler.user_service.customException.UnauthorizedException;
import com.twowheeler.user_service.customException.UserNotFoundException;
import com.twowheeler.user_service.dto.*;
import com.twowheeler.user_service.kafka.UserEventProducer;
import com.twowheeler.user_service.repository.UserProfileRepository;

import lombok.extern.slf4j.Slf4j;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;

import reactor.core.publisher.Mono;

import java.time.Instant;
@Slf4j
@Component
public class UserHandler {


    private final UserProfileRepository repository;
    private final UserEventProducer statusProducer;

    public UserHandler(
            UserProfileRepository repository,
            UserEventProducer statusProducer
    ) {
        this.repository = repository;
        this.statusProducer = statusProducer;
    }

    public Mono<ServerResponse> welcome(ServerRequest request){

        log.info("Application started and Welcome to the screen");
        return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Hello all this is user service");
    }


    /* ================= UPDATE PROFILE ================= */

    public Mono<ServerResponse> updateProfile(ServerRequest request) {

        log.info("USER_UPDATE_PROFILE_START");

        String userId = request.headers().firstHeader("X-User-Id");
        String roleHeader = request.headers().firstHeader("X-User-Role");

        if (userId == null || roleHeader == null) {
            log.warn("USER_UPDATE_PROFILE_UNAUTHORIZED missing_headers=true");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        log.info("USER_UPDATE_PROFILE_REQUEST userId={} role={}", userId, roleHeader);

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

                                    /* ROLE CHANGE */
                                    if (req.role() != null
                                            && !existing.getRole().equals(req.role())) {

                                        log.info(
                                                "USER_ROLE_CHANGE_DETECTED userId={} oldRole={} newRole={}",
                                                userId, existing.getRole(), req.role());

                                        existing.setRole(req.role());
                                        roleChanged = true;
                                    }

                                    /* STATUS CHANGE */
                                    if (req.status() != null
                                            && !existing.getStatus().equals(req.status())) {

                                        log.info(
                                                "USER_STATUS_CHANGE_DETECTED userId={} oldStatus={} newStatus={}",
                                                userId, existing.getStatus(), req.status());

                                        existing.setStatus(req.status());
                                        statusChanged = true;
                                    }

                                    /* PROFILE UPDATES */
                                    if (req.name() != null) existing.setName(req.name());
                                    if (req.email() != null) existing.setEmail(req.email());
                                    if (req.phone() != null) existing.setPhone(req.phone());
                                    if (req.accountType() != null) existing.setAccountType(req.accountType());
                                    if (req.userCategory() != null) existing.setUserCategory(req.userCategory());

                                    existing.setUpdatedAt(Instant.now());

                                    boolean shouldPublish = roleChanged || statusChanged;

                                    return repository.update(existing)
                                            .doOnSuccess(saved -> {
                                                log.info(
                                                        "USER_PROFILE_UPDATED_SUCCESS userId={}",
                                                        userId);

                                                if (shouldPublish) {
                                                    log.info(
                                                            "USER_STATUS_OR_ROLE_EVENT_PUBLISHED userId={}",
                                                            userId);

                                                    statusProducer.publishStatusOrRoleChange(
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
                        log.error(
                                "USER_UPDATE_PROFILE_FAILED userId={} reason={}",
                                userId, e.getMessage(), e));
    }

    /* ================= GET ME ================= */

    public Mono<ServerResponse> getMe(ServerRequest request) {

        log.info("USER_GET_ME_START");

        String userId = request.headers().firstHeader("X-User-Id");

        if (userId == null) {
            log.warn("USER_GET_ME_UNAUTHORIZED missing_userId=true");
            return Mono.error(new UnauthorizedException("Missing authentication headers"));
        }

        log.info("USER_GET_ME_REQUEST userId={}", userId);

        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new UserNotFoundException("User profile not found")))
                .flatMap(profile -> {
                    log.info("USER_GET_ME_SUCCESS userId={}", userId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new UserProfileResponse(
                                    profile.getUserId(),
                                    profile.getName(),
                                    profile.getEmail(),
                                    profile.getPhone(),
                                    profile.getRole(),
                                    profile.getAccountType(),
                                    profile.getUserCategory()
                            ));
                })
                .doOnError(e ->
                        log.error(
                                "USER_GET_ME_FAILED userId={} reason={}",
                                userId, e.getMessage(), e));
    }

    /* ================= GET ALL USERS ================= */

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {

        log.info("USER_GET_ALL_START");

        String role = request.headers().firstHeader("X-User-Role");

        if (role == null) {
            log.warn("USER_GET_ALL_UNAUTHORIZED missing_role=true");
            return Mono.error(new UnauthorizedException("Missing role header"));
        }

        if (!role.equals("ADMIN") && !role.equals("OPERATOR")) {
            log.warn("USER_GET_ALL_FORBIDDEN role={}", role);
            return Mono.error(new UnauthorizedException("Access denied"));
        }

        log.info("USER_GET_ALL_REQUEST role={}", role);

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
                .flatMap(list -> {
                    log.info("USER_GET_ALL_SUCCESS count={}", list.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(list);
                })
                .doOnError(e ->
                        log.error(
                                "USER_GET_ALL_FAILED reason={}",
                                e.getMessage(), e));
    }
}
