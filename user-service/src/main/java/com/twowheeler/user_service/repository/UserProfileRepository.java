package com.twowheeler.user_service.repository;

import com.twowheeler.user_service.model.UserProfile;

import reactor.core.publisher.Mono;

public interface UserProfileRepository {
Mono<UserProfile> save(UserProfile profile);
    Mono<UserProfile> findByUserId(String userId);
}
