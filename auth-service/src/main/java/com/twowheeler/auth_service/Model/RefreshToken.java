package com.twowheeler.auth_service.Model;

import com.twowheeler.auth_service.Annotation.DynamoTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoTable(tableName = "refresh_tokens")
@DynamoDbBean
public class RefreshToken {

    private String token;
    private String userId;
    private Long expiryTime;
    private Long createdAt;

    @DynamoDbPartitionKey
    public String getToken() {
        return token;
    }
}
