package com.example.ec2.service;

import com.example.ec2.dao.ImageMetadata;
import com.example.ec2.entity.ImageEntity;
import com.example.ec2.repository.DynamoDbRepository;
import com.example.ec2.repository.ImageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final LambdaClient lambdaClient;
    private final DynamoDbRepository dynamoDbRepository;
    private final ImageRepository imageRepository;
    private final ObjectMapper objectMapper;

    @Value("${bucket.name}")
    private String bucketName;

    @Value("${sqs-queue.url}")
    private String sqsQueueUrl;

    @Value("${lambda.data-consistency.arn}")
    private String lambdaDataConsistencyArn;

    @Value("${load-balancer.host}")
    private String loadBalancerHost;

    @Value("${load-balancer.port}")
    private String loadBalancerPort;

    @Override
    public InputStream getImageByName(String name) {
        log.info("Getting image by name {}", name);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(name)
                .build();
        dynamoDbRepository.incrementDownloadCount(name);
        log.info("Getting image by name {} was successful", name);
        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public boolean uploadImage(MultipartFile multipartFile) {
        log.info("Uploading image with name {}", multipartFile.getOriginalFilename());
        ImageEntity imageEntity = ImageEntity.builder()
                .name(multipartFile.getOriginalFilename())
                .fileExtension(multipartFile.getOriginalFilename()
                        .substring(multipartFile.getOriginalFilename().lastIndexOf('.') + 1))
                .updateTime(LocalDateTime.now())
                .size(multipartFile.getSize())
                .build();
        imageRepository.save(imageEntity);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .build();
        try {
            s3Client.putObject(request,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
        } catch (Exception e) {
            log.error("Error when putting object to bucket: ", e);
            return false;
        }

        try {
            String objectJson = objectMapper.writeValueAsString(convertImageEntityToImageMetadata(imageEntity));
            System.out.println("objectJson: " + objectJson);
            ImageMetadata imageMetadata = convertImageEntityToImageMetadata(imageEntity);
            imageMetadata.setApplicationUrl(loadBalancerHost + ":" + loadBalancerPort);
            SendMessageRequest sqsRequest = SendMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .messageBody(objectMapper.writeValueAsString(imageMetadata))
                    .build();
            sqsClient.sendMessage(sqsRequest);
        } catch (Exception e) {
            log.error("Error when sending message to sqs: ", e);
            return false;
        }
        log.info("Uploading image with name {} was successful", multipartFile.getOriginalFilename());
        return true;
    }

    @Override
    @Transactional
    public boolean deleteImageByName(String name) {
        log.info("Deleting image by name {}", name);
        imageRepository.deleteByName(name);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(name)
                .build();
        try {
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.error("Error when putting object to bucket: ", e);
            return false;
        }
        log.info("Deleting image by name {} was successful", name);
        return true;
    }

    @Override
    public ImageMetadata getImageMetadataByName(String name) {
        log.info("Getting image metadata by name {}", name);
        ImageEntity imageEntity = imageRepository.findByName(name);
        dynamoDbRepository.incrementViewCount(name);
        log.info("Getting image metadata by name {} was successful", name);
        return convertImageEntityToImageMetadata(imageEntity);
    }

    @Override
    public ImageMetadata getRandomImageMetadata() {
        log.info("Getting random image metadata by name");
        long count = imageRepository.count();
        if (count == 0) return null;
        int randomIndex = new Random().nextInt((int) count);
        Page<ImageEntity> imageEntityPage = imageRepository.findAll(PageRequest.of(randomIndex, 1));
        log.info("Getting random image metadata by name was successful");
        return imageEntityPage.getContent().isEmpty() ?
                null : convertImageEntityToImageMetadata(imageEntityPage.getContent().get(0));
    }

    @Override
    public String checkConsistentState() {
        log.info("Checking consistent state");
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(lambdaDataConsistencyArn)
                .build();
        InvokeResponse response = lambdaClient.invoke(invokeRequest);
        log.info("Successfully checked consistent state");
        return response.payload().asString(StandardCharsets.UTF_8);
    }

    private ImageMetadata convertImageEntityToImageMetadata(ImageEntity imageEntity) {
        return ImageMetadata.builder()
                .name(imageEntity.getName())
                .fileExtension(imageEntity.getFileExtension())
                .updateTime(imageEntity.getUpdateTime())
                .size(imageEntity.getSize())
                .build();
    }

}
