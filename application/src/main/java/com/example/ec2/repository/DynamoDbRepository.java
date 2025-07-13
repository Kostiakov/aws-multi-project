package com.example.ec2.repository;

public interface DynamoDbRepository {

    void incrementViewCount(String imageName);

    void incrementDownloadCount(String imageName);

}
