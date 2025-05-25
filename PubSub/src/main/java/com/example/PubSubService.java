package com.example;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubService {
    private final PubSubTemplate pubSubTemplate;
    private final String topicName = "example-topic";

    public void publishMessage(String message) {
        message = message.toUpperCase();
        log.info("Publishing message: '{}' to topic: '{}'", message, topicName);
        pubSubTemplate.publish(topicName, message);
    }
}
