package com.example;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for publishing messages to Google Cloud Pub/Sub.
 */
@Service
@RequiredArgsConstructor // Automatically injects `PubSubTemplate` via constructor.
@Slf4j // Enables logging.
public class PubSubService {
    private final PubSubTemplate pubSubTemplate;
    private final String topicName = "example-topic"; // Defines the target Pub/Sub topic.

    /**
     * Publishes a message to the configured Pub/Sub topic.
     * The message is converted to uppercase before publishing.
     * @param message The message string to be published.
     */
    public void publishMessage(String message) {
        message = message.toUpperCase(); // Applies a simple transformation (e.g., business logic).
        log.info("Publishing message: '{}' to topic: '{}'", message, topicName);
        pubSubTemplate.publish(topicName, message); // Delegates to the Spring Cloud GCP Pub/SubTemplate.
    }
}