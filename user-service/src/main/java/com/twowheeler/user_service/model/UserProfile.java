package com.twowheeler.user_service.model;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
@Data
@DynamoDbBean
@DynamoEntity
public class UserProfile {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Long createdAt;
    private Long updatedAt;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
}
