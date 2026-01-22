package com.twowheeler.user_service.repository;

import com.twowheeler.user_service.model.UserProfile;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserProfileRepository {
Mono<UserProfile> save(UserProfile profile);
    Mono<UserProfile> findByUserId(String userId);
    Mono<UserProfile> update(UserProfile profile);
    Flux<UserProfile> findAll();
}
