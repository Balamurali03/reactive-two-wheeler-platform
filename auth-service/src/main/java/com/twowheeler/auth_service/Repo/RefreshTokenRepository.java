package com.twowheeler.auth_service.Repo;

import com.twowheeler.auth_service.Model.RefreshToken;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepository {

    Mono<Void> save(RefreshToken token);

    Mono<RefreshToken> find(String token);

    Mono<Void> delete(String token);
}
