package com.twowheeler.auth_service.RepoImpl;

import java.time.Instant;


import org.springframework.stereotype.Repository;


import com.twowheeler.auth_service.Enum.Status;
import com.twowheeler.auth_service.Model.UserStatus;
import com.twowheeler.auth_service.Repo.UserStatusRepository;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Repository
public class UserStatusRepositoryImpl implements UserStatusRepository {

    private final DynamoDbTable<UserStatus> userStatusTable;

    public UserStatusRepositoryImpl(DynamoDbTable<UserStatus> userStatusTable) {
        this.userStatusTable = userStatusTable;
    }

    /* ================= SAVE ================= */

    @Override
    public Mono<UserStatus> save(UserStatus userStatus) {
        return Mono.fromCallable(() -> {
            userStatusTable.putItem(
                    PutItemEnhancedRequest.builder(UserStatus.class)
                            .item(userStatus)
                            .build()
            );
            return userStatus;
        });
    }

    /* ================= FIND BY ID ================= */

   @Override
public Mono<UserStatus> findById(String userId) {
    return Mono.fromCallable(() ->
            userStatusTable.getItem(
                    Key.builder()
                            .partitionValue(userId)
                            .build()
            )
    ).flatMap(userStatus -> {
        if (userStatus == null) {
            return Mono.empty();
        }
        return Mono.just(userStatus);
    });
}


    /* ================= UPDATE STATUS ================= */

    @Override
    public Mono<UserStatus> updateStatus(String userId, Status newStatus) {
        return findById(userId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("UserStatus not found for userId=" + userId)))
                .flatMap(existing -> {

                    existing.setStatus(newStatus);
                    existing.setUpdatedAt(Instant.now());

                    return Mono.fromCallable(() -> {
                        userStatusTable.updateItem(existing);
                        return existing;
                    });
                });
    }

}
