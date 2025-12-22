package com.twowheeler.auth_service.RepoImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.twowheeler.auth_service.CustomException.DuplicateUsernameException;
import com.twowheeler.auth_service.Model.User;
import com.twowheeler.auth_service.Repo.UserRepository;

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
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private DynamoDbTable<User> userTable;

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return Mono.fromSupplier(() ->{
        	DynamoDbIndex<User> index = userTable.index("username-index");
        	SdkIterable<Page<User>> pages =
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
	public Mono<User> save(User user) {
		
		return Mono.fromCallable(() -> {
			PutItemEnhancedRequest<User> request =
			PutItemEnhancedRequest.builder(User.class)
			.item(user).conditionExpression(
					Expression.builder()
					.expression("attribute_not_exists(username)")
					.build())
			.build();
			userTable.putItem(request);
			return user;
		});
	}



	@Override
	public Mono<User> findByUsername(String username) {
		return Mono.fromCallable(() -> {
			DynamoDbIndex<User> index = userTable.index("username-index");
			
			SdkIterable<Page<User>> pages =
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
	public Mono<User> findById(String userId) {
		return Mono.fromCallable(() ->
			userTable.getItem(
					Key.builder()
					.partitionValue(userId)
					.build())
		).flatMap(Mono :: justOrEmpty);
	}

}
