package com.twowheeler.auth_service.Model;

import com.twowheeler.auth_service.Annotation.DynamoTable;
import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Enum.Status;

import lombok.*;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoTable(tableName = "users")
@DynamoDbBean
public class User {

    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String phoneNum;
    private Roles role;
    private Status status;
    private Long createdAt;
    private Long updatedAt;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }
}