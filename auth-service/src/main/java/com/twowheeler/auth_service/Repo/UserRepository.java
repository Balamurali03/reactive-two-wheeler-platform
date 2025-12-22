package com.twowheeler.auth_service.Repo;

import com.twowheeler.auth_service.Model.User;

import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<User> findByUsername(String username);

    Mono<User> findById(String userId);

    Mono<Boolean> existsByUsername(String username);

}
