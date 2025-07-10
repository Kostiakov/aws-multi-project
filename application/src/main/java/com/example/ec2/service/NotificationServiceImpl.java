package com.example.ec2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SnsClient snsClient;

    @Value("${sns-topic.arn}")
    private String snsTopicArn;

    @Override
    public boolean registerEmail(String email) {
        log.info("Creating subscription for email {}", email);
        SubscribeRequest request = SubscribeRequest.builder()
                .protocol("email")
                .endpoint(email)
                .returnSubscriptionArn(true)
                .topicArn(snsTopicArn)
                .build();
        try {
            snsClient.subscribe(request);
        } catch (Exception e) {
            log.error("Error when creating subscription: ", e);
            return false;
        }
        log.info("Subscription successfully created for email {}", email);
        return true;
    }

    @Override
    public boolean unregisterEmail(String email) {
        log.info("Unsubscribing email {}", email);
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
                .topicArn(snsTopicArn)
                .build();

        ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(request);

        for (Subscription subscription : response.subscriptions()) {
            if (subscription.endpoint().equalsIgnoreCase(email)) {
                String subscriptionArn = subscription.subscriptionArn();
                try {
                    snsClient.unsubscribe(UnsubscribeRequest.builder()
                            .subscriptionArn(subscriptionArn)
                            .build());
                } catch (Exception e) {
                    log.error("Error when trying to unsubscribe: ", e);
                    return false;
                }
                log.info("Email unsubscribed successfully {}", email);
                return true;
            }
        }
        log.info("Email {} not found to unsubscribe", email);
        return false;
    }
}
