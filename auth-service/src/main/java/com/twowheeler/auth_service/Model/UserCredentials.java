package com.twowheeler.auth_service.Model;

import java.time.Instant;


import com.twowheeler.auth_service.Annotation.DynamoTable;
import com.twowheeler.auth_service.Enum.Roles;

import lombok.*;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoTable(tableName = "UserCredentials")
@DynamoDbBean
public class UserCredentials {

    private String userId;
    private String username;
    private String passwordHash;
    private Roles role;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }
}