package com.twowheeler.auth_service.RepoImpl;



import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.twowheeler.auth_service.CustomException.DuplicateUsernameException;
import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Model.UserCredentials;
import com.twowheeler.auth_service.Repo.UserCredentialsRepository;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
//import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class UserCredentialsRepositoryImpl implements UserCredentialsRepository {

    @Autowired
    private DynamoDbTable<UserCredentials> userCredentialsTable;

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return Mono.fromSupplier(() ->{
        	DynamoDbIndex<UserCredentials> index = userCredentialsTable.index("username-index");
        	SdkIterable<Page<UserCredentials>> pages =
        			index.query(
        					QueryConditional.keyEqualTo(
        							Key.builder().partitionValue(username)
        							.build()));
        	return pages.stream()
        			.flatMap(page ->page.items().stream())
        			.findFirst()
        			.isPresent();
        });
    }



	@Override
	public Mono<UserCredentials> save(UserCredentials userCredentials) {
		
		return Mono.fromCallable(() -> {
			PutItemEnhancedRequest<UserCredentials> request =
			PutItemEnhancedRequest.builder(UserCredentials.class)
			.item(userCredentials).conditionExpression(
					Expression.builder()
					.expression("attribute_not_exists(UserCredentialsname)")
					.build())
			.build();
			userCredentialsTable.putItem(request);
			return userCredentials;
		});
	}



	@Override
	public Mono<UserCredentials> findByUsername(String username) {
		return Mono.fromCallable(() -> {
			DynamoDbIndex<UserCredentials> index = userCredentialsTable.index("username-index");
			
			SdkIterable<Page<UserCredentials>> pages =
					index.query(
							QueryConditional.keyEqualTo(
									Key.builder()
									.partitionValue(username)
									.build())
							);
			return pages.stream()
					.flatMap(page -> page.items().stream())
					.reduce((a,b) -> {
						throw new DuplicateUsernameException(username);
					}) .orElse(null);
		}).flatMap(Mono :: justOrEmpty);
	}



	@Override
	public Mono<UserCredentials> findById(String userId) {
		return Mono.fromCallable(() ->
			userCredentialsTable.getItem(
					Key.builder()
					.partitionValue(userId.toString())
					.build())
		).flatMap(Mono :: justOrEmpty);
	}



	@Override
	public Mono<UserCredentials> updateRole(String userId, Roles newRole) {
		return findById(userId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("UserStatus not found for userId=" + userId)))
                .flatMap(existing -> {

                    existing.setRole(newRole);
                    existing.setUpdatedAt(Instant.now());

                    return Mono.fromCallable(() -> {
                        userCredentialsTable.updateItem(existing);
                        return existing;
                    });
                });
	}

	
}
