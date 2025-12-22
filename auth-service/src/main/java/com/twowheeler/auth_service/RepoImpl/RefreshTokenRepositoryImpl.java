package com.twowheeler.auth_service.RepoImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.twowheeler.auth_service.Model.RefreshToken;
import com.twowheeler.auth_service.Repo.RefreshTokenRepository;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    @Autowired
    private DynamoDbTable<RefreshToken> refreshTokenTable;
    
    public Mono<Void> save(RefreshToken token) {
        return Mono.fromRunnable(()-> refreshTokenTable.putItem(token));
    }

    public Mono<RefreshToken> find(String token) {
        return Mono.fromCallable(() -> refreshTokenTable.getItem(Key.builder().partitionValue(token).build()));
    }

    public Mono<Void> delete(String token) {
        return Mono.fromRunnable(() ->
        refreshTokenTable.deleteItem(Key.builder().partitionValue(token).build())
        );
    } 
}
