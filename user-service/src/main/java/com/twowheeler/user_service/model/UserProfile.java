package com.twowheeler.user_service.model;

import java.time.Instant;

import com.twowheeler.user_service.enums.AccountType;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.UserCategory;
import com.twowheeler.user_service.enums.UserRole;

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
    private UserRole role;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
    private AccountType accountType;
    private UserCategory userCategory;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
}
