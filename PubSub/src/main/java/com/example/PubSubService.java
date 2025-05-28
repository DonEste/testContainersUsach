package com.example;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for publishing messages to Google Cloud Pub/Sub.
 */
@Service
public class PubSubService {

    @Autowired
    private final PubSubTemplate pubSubTemplate;
    private final String topicName = "example-topic"; // Defines the target Pub/Sub topic.

    public PubSubService(PubSubTemplate pubSubTemplate) {
        this.pubSubTemplate = pubSubTemplate;
    }

    /**
     * Publishes a message to the configured Pub/Sub topic.
     * The message is converted to uppercase before publishing.
     *
     * @param message The message string to be published.
     */
    public void publishMessage(String message) {
        message = message.toUpperCase(); // Applies a simple transformation (e.g., business logic).
        System.out.printf("Publishing message: '%s' to topic: '%s'%n", message, topicName);
        pubSubTemplate.publish(topicName, message); // Delegates to the Spring Cloud GCP Pub/SubTemplate.
    }
}