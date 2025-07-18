package com.example.ec2.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbRepositoryImpl implements DynamoDbRepository {

    private final DynamoDbClient dynamoDbClient;

    @Value("${dynamodb.table}")
    private String tableName;

    private static final String VIEW_COUNT = "view_count";
    private static final String DOWNLOAD_COUNT = "download_count";

    @Override
    public void incrementViewCount(String imageName) {
        incrementCount(imageName, VIEW_COUNT);
    }

    @Override
    public void incrementDownloadCount(String imageName) {
        incrementCount(imageName, DOWNLOAD_COUNT);
    }

    private void incrementCount(String imageName, String countName) {
        Map<String, AttributeValue> key = Map.of(
                "id", AttributeValue.builder().s(imageName).build()
        );
        String timeStamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":incr", AttributeValue.builder().n("1").build());
        expressionValues.put(":zero", AttributeValue.builder().n("0").build());
        expressionValues.put(":timestamp", AttributeValue.builder().s(timeStamp).build());
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET " + countName + " = if_not_exists(" + countName + ", :zero) + :incr, " +
                        "last_updated = :timestamp")
                .expressionAttributeValues(expressionValues)
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();
        try {
            dynamoDbClient.updateItem(request);
        } catch (Exception e) {
            log.error("Exception while updating analytics: ", e);
        }
    }

}
