package com.twowheeler.auth_service.Config;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.twowheeler.auth_service.Annotation.DynamoTable;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Set;

@Slf4j
@Configuration
public class DynamoAutoTableCreator {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${app.dynamodb.create-table:false}")
    private boolean createTable;

    public DynamoAutoTableCreator(
            DynamoDbClient dynamoDbClient,
            DynamoDbEnhancedClient enhancedClient
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.enhancedClient = enhancedClient;
    }

    @PostConstruct
    public void createTables() {

        if (!createTable) return;

        Reflections reflections =
                new Reflections("com.twowheeler.auth_service.Model");

        Set<Class<?>> tables =
                reflections.getTypesAnnotatedWith(DynamoTable.class);

        tables.forEach(this::createTableIfMissing);
    }

    private void createTableIfMissing(Class<?> clazz) {

        DynamoTable annotation = clazz.getAnnotation(DynamoTable.class);
        String tableName = annotation.tableName();

        try {
            dynamoDbClient.describeTable(
                    DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build()
            );
            log.info("✅ Table {} exists", tableName);

        } catch (ResourceNotFoundException e) {

            log.info("🛠 Creating table {}", tableName);

            DynamoDbTable<?> table =
                    enhancedClient.table(
                            tableName,
                            TableSchema.fromBean(clazz)
                    );

            table.createTable();

            log.info("🎉 Table {} created", tableName);
        }
    }
}

