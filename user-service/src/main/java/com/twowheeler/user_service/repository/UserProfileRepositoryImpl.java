package com.twowheeler.user_service.repository;

import org.springframework.stereotype.Repository;

import com.twowheeler.user_service.model.UserProfile;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final DynamoDbAsyncTable<UserProfile> table;

    public UserProfileRepositoryImpl(DynamoDbAsyncClient client) {
        this.table = DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build()
                .table("UserProfile", TableSchema.fromBean(UserProfile.class));
    }

    public Mono<UserProfile> save(UserProfile profile) {
    return Mono.fromRunnable(() -> table.putItem(profile))
               .thenReturn(profile);
}


    public Mono<UserProfile> findByUserId(String userId) {
        return Mono.fromFuture(
                table.getItem(Key.builder().partitionValue(userId).build())
        );
    }
}

