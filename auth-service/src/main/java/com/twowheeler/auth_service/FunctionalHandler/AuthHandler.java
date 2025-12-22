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

import com.twowheeler.auth_service.CustomException.DuplicateUsernameException;
import com.twowheeler.auth_service.CustomException.InvalidCredentialsException;
import com.twowheeler.auth_service.CustomException.TokenExpiredException;
import com.twowheeler.auth_service.Dto.Request.LoginRequest;
import com.twowheeler.auth_service.Dto.Request.RefreshTokenRequest;
import com.twowheeler.auth_service.Dto.Request.RegisterRequest;
import com.twowheeler.auth_service.Dto.Response.AuthResponse;
import com.twowheeler.auth_service.Enum.Status;
import com.twowheeler.auth_service.Mapper.UserMapper;
import com.twowheeler.auth_service.Model.RefreshToken;
import com.twowheeler.auth_service.Model.User;
import com.twowheeler.auth_service.Repo.UserRepository;
import com.twowheeler.auth_service.Repo.RefreshTokenRepository;
import com.twowheeler.auth_service.Security.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthHandler {

    @Autowired
    private UserRepository repo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshRepo;

    // private static final long REFRESH_EXPIRY = 7 * 24 * 60 * 60 * 1000L; // 7
    // days

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterRequest.class)
                .doOnSubscribe(s -> log.info("➡️  REGISTER request received"))
                .doOnNext(r -> log.info("👤 Register username={}", r.username()))
                .flatMap(req -> repo.existsByUsername(req.username())
                        .doOnNext(exists -> log.debug("🔍 Username '{}' exists? {}", req.username(), exists))
                        .flatMap(exists -> {
                            if (exists) {
                                log.warn("❌ Duplicate username attempt: {}", req.username());
                                return Mono.error(
                                        new DuplicateUsernameException("Username already exists"));
                            }

                            User user = new User(
                                    UUID.randomUUID().toString(),
                                    req.username(),
                                    encoder.encode(req.password()),
                                    req.email(),
                                    req.phoneNumber(),
                                    req.role(),
                                    Status.ACTIVE,
                                    System.currentTimeMillis(),
                                    System.currentTimeMillis());

                            log.info("🛠 Creating user id={}", user.getUserId());
                            return repo.save(user)
                                    .doOnSuccess(u -> log.info("✅ User registered successfully: {}", u.getUsername()))
                                    .map(UserMapper::response);

                        }))
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e -> log.error("🔥 REGISTER failed", e));

    }

    /* ================= LOGIN ================= */

    public Mono<ServerResponse> login(ServerRequest request) {

        return request.bodyToMono(LoginRequest.class)
                .doOnSubscribe(s -> log.info("➡️  LOGIN request received"))
                .doOnNext(r -> log.info("🔐 Login attempt username={}", r.username()))
                .flatMap(req -> repo.findByUsername(req.username())
                        .switchIfEmpty(Mono.error(
                                new InvalidCredentialsException("Invalid credentials")))
                        .flatMap(user -> {
                            if (!encoder.matches(req.password(), user.getPasswordHash())) {
                                log.warn("❌ Invalid password for username={}", req.username());
                                return Mono.error(
                                        new InvalidCredentialsException("Invalid credentials"));
                            }

                            log.info("✅ Login success for username={}", req.username());

                            String accessToken = jwtUtil.generateAccessToken(
                                    user.getUsername(),
                                    List.of(user.getRole().name()));

                            String refreshToken = UUID.randomUUID().toString();

                            long expiry = Instant.now()
                                    .plus(7, ChronoUnit.DAYS)
                                    .getEpochSecond();

                            RefreshToken token = new RefreshToken(
                                    refreshToken,
                                    user.getUserId(),
                                    expiry,
                                    System.currentTimeMillis());

                            log.debug("♻️ Refresh token issued for userId={}", user.getUserId());

                            return refreshRepo.save(token)
                                    .thenReturn(
                                            new AuthResponse(accessToken, refreshToken));
                        }))
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e -> log.error("🔥 LOGIN failed", e));
    }

    /* ================= REFRESH ================= */

    public Mono<ServerResponse> refresh(ServerRequest request) {

        return request.bodyToMono(RefreshTokenRequest.class)
                .doOnSubscribe(s -> log.info("➡️  REFRESH request received"))
                .flatMap(req -> refreshRepo.find(req.refreshToken())
                        .switchIfEmpty(Mono.error(
                                new InvalidCredentialsException("Invalid refresh token")))
                        .flatMap(token -> {
                            if (token.getExpiryTime() < Instant.now().getEpochSecond()) {
                                log.warn("⏰ Refresh token expired: {}", token.getToken());
                                return refreshRepo.delete(token.getToken())
                                        .then(Mono.error(
                                                new TokenExpiredException("Refresh token expired")));
                            }

                            log.info("♻️ Refresh token valid for userId={}", token.getUserId());

                            return repo.findById(token.getUserId())
                                    .map(user -> new AuthResponse(
                                            jwtUtil.generateAccessToken(
                                                    user.getUsername(),
                                                    List.of(user.getRole().name())),
                                            token.getToken()));
                        }))
                .flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resp))
                .doOnError(e -> log.error("🔥 REFRESH failed", e));
    }

}
