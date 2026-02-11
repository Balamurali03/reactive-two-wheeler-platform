package com.twowheeler.auth_service.FunctionalHandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.twowheeler.auth_service.CustomException.AccessDeniedException;
import com.twowheeler.auth_service.CustomException.DuplicateUsernameException;
import com.twowheeler.auth_service.CustomException.InvalidCredentialsException;
import com.twowheeler.auth_service.CustomException.TokenExpiredException;
import com.twowheeler.auth_service.CustomException.UserBlockedException;
import com.twowheeler.auth_service.Dto.Request.LoginRequest;
import com.twowheeler.auth_service.Dto.Request.RefreshTokenRequest;
import com.twowheeler.auth_service.Dto.Request.RegisterRequest;
import com.twowheeler.auth_service.Dto.Response.AuthResponse;
import com.twowheeler.auth_service.Dto.Response.UserResponse;
import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Enum.Status;
import com.twowheeler.auth_service.Event.AuthEventProducer;
import com.twowheeler.auth_service.Model.RefreshToken;
import com.twowheeler.auth_service.Model.UserCredentials;
import com.twowheeler.auth_service.Model.UserStatus;
import com.twowheeler.auth_service.Repo.RefreshTokenRepository;
import com.twowheeler.auth_service.Repo.UserCredentialsRepository;
import com.twowheeler.auth_service.Repo.UserStatusRepository;
import com.twowheeler.auth_service.Security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthHandler {

    @Autowired
    private UserCredentialsRepository credentialsRepo;
    @Autowired
    private UserStatusRepository statusRepo;
    @Autowired
    private RefreshTokenRepository refreshTokenRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthEventProducer eventProducer;

    public Mono<ServerResponse> welcome(ServerRequest request){

        log.info("Application started and Welcome to the screen");
        return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Hello all this is auth service");
    }

    /* ================= REGISTER ================= */

    public Mono<ServerResponse> register(ServerRequest request) {

        log.info("AUTH_REGISTER_START");

        return request.bodyToMono(RegisterRequest.class)
                .doOnNext(req ->
                        log.info("AUTH_REGISTER_REQUEST username={}", req.username()))
                .flatMap(req -> {

                    if (req.role() != null && req.role() != Roles.USER) {
                        log.warn("AUTH_REGISTER_FORBIDDEN_ROLE username={} role={}",
                                req.username(), req.role());
                        return Mono.error(new AccessDeniedException(
                                "Only USER role can be self-registered"));
                    }

                    return credentialsRepo.existsByUsername(req.username())
                            .flatMap(exists -> {
                                if (exists) {
                                    log.warn("AUTH_REGISTER_DUPLICATE username={}", req.username());
                                    return Mono.error(new DuplicateUsernameException(
                                            "Username already exists"));
                                }

                                String userId = UUID.randomUUID().toString();
                                Instant now = Instant.now();

                                UserCredentials credentials = new UserCredentials(
                                        userId,
                                        req.username(),
                                        passwordEncoder.encode(req.password()),
                                        Roles.USER,
                                        now,
                                        now
                                );

                                UserStatus status = new UserStatus(
                                        userId,
                                        Status.ACTIVE,
                                        now
                                );

                                return credentialsRepo.save(credentials)
                                        .then(statusRepo.save(status))
                                        .doOnSuccess(v -> {
                                            log.info("AUTH_REGISTER_SUCCESS userId={} username={}",
                                                    userId, req.username());
                                            eventProducer.publishUserRegistered(userId,req.username(),Roles.USER);
                                        })
                                        .thenReturn(new UserResponse(
                                                userId,
                                                req.username(),
                                                Roles.USER,
                                                Instant.now()
                                        ));
                            });
                })
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e ->
                        log.error("AUTH_REGISTER_FAILED reason={}", e.getMessage(), e));
    }

    /* ================= LOGIN ================= */

    public Mono<ServerResponse> login(ServerRequest request) {

        log.info("AUTH_LOGIN_START");

        return request.bodyToMono(LoginRequest.class)
                .doOnNext(req ->
                        log.info("AUTH_LOGIN_REQUEST username={}", req.username()))
                .flatMap(req ->
                        credentialsRepo.findByUsername(req.username())
                                .switchIfEmpty(Mono.error(
                                        new InvalidCredentialsException("Invalid credentials")))
                                .flatMap(credentials ->
                                        statusRepo.findById(credentials.getUserId())
                                                .switchIfEmpty(Mono.error(
                                                        new UserBlockedException("User status not found")))
                                                .flatMap(status -> {

                                                    if (status.getStatus() != Status.ACTIVE) {
                                                        log.warn("AUTH_LOGIN_BLOCKED userId={} status={}",
                                                                credentials.getUserId(), status.getStatus());
                                                        return Mono.error(
                                                                new UserBlockedException(
                                                                        "User is " + status.getStatus()));
                                                    }

                                                    if (!passwordEncoder.matches(
                                                            req.password(),
                                                            credentials.getPasswordHash())) {
                                                        log.warn("AUTH_LOGIN_INVALID_PASSWORD username={}",
                                                                req.username());
                                                        return Mono.error(
                                                                new InvalidCredentialsException("Invalid credentials"));
                                                    }

                                                    String accessToken =
                                                            jwtUtil.generateAccessToken(
                                                                    credentials.getUsername(),
                                                                    List.of(credentials.getRole().name()));

                                                    String refreshToken = UUID.randomUUID().toString();
                                                    long expiry =
                                                            Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond();

                                                    RefreshToken token = new RefreshToken(
                                                            refreshToken,
                                                            credentials.getUserId(),
                                                            expiry,
                                                            Instant.now()
                                                    );

                                                    return refreshTokenRepo.save(token)
                                                            .doOnSuccess(v ->
                                                                    log.info(
                                                                            "AUTH_LOGIN_SUCCESS userId={} role={}",
                                                                            credentials.getUserId(),
                                                                            credentials.getRole()))
                                                            .thenReturn(new AuthResponse(
                                                                    credentials.getUserId(),
                                                                    credentials.getRole().name(),
                                                                    accessToken,
                                                                    refreshToken));
                                                })
                                ))
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e ->
                        log.error("AUTH_LOGIN_FAILED reason={}", e.getMessage(), e));
    }

    /* ================= REFRESH ================= */

    public Mono<ServerResponse> refresh(ServerRequest request) {

        log.info("AUTH_REFRESH_START");

        return request.bodyToMono(RefreshTokenRequest.class)
                .doOnNext(req ->
                        log.info("AUTH_REFRESH_REQUEST token={}", req.refreshToken()))
                .flatMap(req ->
                        refreshTokenRepo.find(req.refreshToken())
                                .switchIfEmpty(Mono.error(
                                        new InvalidCredentialsException("Invalid refresh token")))
                                .flatMap(token -> {

                                    if (token.getExpiryTime() < Instant.now().getEpochSecond()) {
                                        log.warn("AUTH_REFRESH_EXPIRED userId={}", token.getUserId());
                                        return refreshTokenRepo.delete(token.getToken())
                                                .then(Mono.error(
                                                        new TokenExpiredException("Refresh token expired")));
                                    }

                                    return credentialsRepo.findById(token.getUserId())
                                            .map(user -> {
                                                log.info("AUTH_REFRESH_SUCCESS userId={}", user.getUserId());
                                                return new AuthResponse(
                                                        user.getUserId(),
                                                        user.getRole().name(),
                                                        jwtUtil.generateAccessToken(
                                                                user.getUsername(),
                                                                List.of(user.getRole().name())),
                                                        token.getToken());
                                            });
                                }))
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e ->
                        log.error("AUTH_REFRESH_FAILED reason={}", e.getMessage(), e));
    }
}
