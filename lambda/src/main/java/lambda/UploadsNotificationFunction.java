package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class UploadsNotificationFunction implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        System.out.println("Input: " + input);
        for (SQSEvent.SQSMessage msg : input.getRecords()) {
            ImageMetadata imageMetadata = null;
            try {
                imageMetadata = objectMapper.readValue(msg.getBody(), ImageMetadata.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            if (imageMetadata != null) {
                System.out.println("ImageMetadata: " + imageMetadata);
                String notificationSubject = "Image was uploaded";
                String notificationMessage = "Image metadata: size: " + imageMetadata.getSize() + ", name: " +
                        imageMetadata.getName() + ", extension: " + imageMetadata.getFileExtension() +
                        ". You can download image from this url: http://" + "localhost:8080" +
                        "/api/v1/image/download?name=" + imageMetadata.getName();
                String topicArn = System.getenv("TOPIC_ARN");
                System.out.println("topicArn: " + topicArn);
                PublishRequest request = PublishRequest.builder()
                        .topicArn(topicArn)
                        .subject(notificationSubject)
                        .message(notificationMessage)
                        .build();
                String accessKeyId = System.getenv("ACCESS_KEY_ID");
                String secretAccessKey = System.getenv("SECRET_ACCESS_KEY");
                System.out.println("accessKeyId " + accessKeyId.charAt(0) + " secretAccessKey " + secretAccessKey.charAt(0));
                SnsClient snsClient = SnsClient.builder()
                        .region(Region.of(System.getenv("REGION")))
                        .credentialsProvider(StaticCredentialsProvider
                                .create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                        .build();
                snsClient.publish(request);
            }
        }
        return null;
    }
}
