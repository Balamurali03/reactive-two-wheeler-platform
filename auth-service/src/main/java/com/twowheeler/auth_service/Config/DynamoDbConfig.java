package com.twowheeler.auth_service.Config;

import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class DynamoDbConfig {

	@Value("${aws.dynamodb.endpoint}")
	private String endpoint;
	@Value("${aws.region}")
	private String region;
	@Value("${aws.access-key}")
	private String accessKey;
	@Value("${aws.secret-key}")
	private String secretKey;
	
    @Bean
    public DynamoDbClient client(){
        return DynamoDbClient.builder()
        		.endpointOverride(URI.create(endpoint))
        		.region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient client){
        return DynamoDbEnhancedClient
        .builder().dynamoDbClient(client()).build();

    }

    @Bean
    public DynamoDbTable<?> userTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("users", TableSchema.fromBean(
                com.twowheeler.auth_service.Model.User.class));

    }
    
    @Bean
    public DynamoDbTable<?> refreshTokenTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("refresh_tokens", TableSchema.fromBean(
                com.twowheeler.auth_service.Model.RefreshToken.class));

    }

}
