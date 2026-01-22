package com.twowheeler.auth_service.Repo;



import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Model.UserCredentials;

import reactor.core.publisher.Mono;

public interface UserCredentialsRepository {

    Mono<UserCredentials> save(UserCredentials user);

    Mono<UserCredentials> findByUsername(String username);

    Mono<UserCredentials> findById(String userId);

    Mono<Boolean> existsByUsername(String username);
    
    Mono<UserCredentials> updateRole(String userId, Roles role);

}
