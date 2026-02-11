package com.twowheeler.auth_service.Model;

import java.time.Instant;


import com.twowheeler.auth_service.Annotation.DynamoTable;
import com.twowheeler.auth_service.Enum.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoTable(tableName = "user_status")
@DynamoDbBean
public class UserStatus {

    private String userId;
    private Status status;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
}
