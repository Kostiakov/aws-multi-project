package com.example.ec2.scheduler;

import com.example.ec2.dao.ImageMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final SqsClient sqsClient;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs-queue.url}")
    private String sqsQueueUrl;

    @Value("${sns-topic.arn}")
    private String snsTopicArn;

    @Value("${load-balancer.host}")
    private String loadBalancerHost;

    @Value("${load-balancer.port}")
    private String loadBalancerPort;

    @Override
    //@Scheduled(fixedRate = 30000)
    public void processMessagesFromSqs() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {
            log.info("Processing: " + message.body());
            try {
                ImageMetadata imageMetadata = objectMapper.readValue(message.body(), ImageMetadata.class);
                processMessage(imageMetadata);
                deleteMessage(message);
            } catch (Exception e) {
                log.error("Failed to process message: ", e);
            }
        }
    }

    private void processMessage(ImageMetadata imageMetadata) {
        String notificationSubject = "Image was uploaded";
        String notificationMessage = "Image metadata: size: " + imageMetadata.getSize() + ", name: " +
                imageMetadata.getName() + ", extension: " + imageMetadata.getFileExtension() +
                ". You can download image from this url: http://" + loadBalancerHost + ":" + loadBalancerPort +
                "/api/v1/image/download?name=" + imageMetadata.getName();
        PublishRequest request = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .subject(notificationSubject)
                .message(notificationMessage)
                .build();
        snsClient.publish(request);
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteRequest);
    }

}
