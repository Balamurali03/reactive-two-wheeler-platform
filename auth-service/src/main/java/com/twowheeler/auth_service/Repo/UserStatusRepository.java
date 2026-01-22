package com.twowheeler.auth_service.Repo;


import com.twowheeler.auth_service.Enum.Status;
import com.twowheeler.auth_service.Model.UserStatus;

import reactor.core.publisher.Mono;

public interface UserStatusRepository {

    Mono<UserStatus> save(UserStatus user);

    Mono<UserStatus> updateStatus(String userId,Status newStatus);

    Mono<UserStatus> findById(String userId);

}
