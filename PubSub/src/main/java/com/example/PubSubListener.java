package com.example;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles incoming Pub/Sub messages, integrating with Spring Integration.
 */
@Component
public class PubSubListener {

    private final String subscriptionName = "example-subscription"; // The Pub/Sub subscription name to listen to.

    private final BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>(); // Stores messages as they arrive.


    /**
     * Defines the Spring Integration channel for incoming Pub/Sub messages.
     */
    @Bean
    public MessageChannel pubsubInputChannel() {
        return new DirectChannel(); // A direct channel processes messages synchronously.
    }

    public BlockingQueue<String> getReceivedMessages() {
        return receivedMessages;
    }

    /**
     * Configures the inbound adapter to pull messages from Pub/Sub and route them to `pubsubInputChannel`.
     * Uses **manual acknowledgment mode** for explicit control over message acknowledgment.
     */
    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("pubsubInputChannel") MessageChannel inputChannel, // Injects the defined input channel.
            PubSubTemplate pubSubTemplate) { // Injects the core Pub/Sub client.
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
        adapter.setOutputChannel(inputChannel);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    /**
     * Processes messages arriving at `pubsubInputChannel`. Extracts payload, logs, stores, and acknowledges the message.
     */
    @Bean
    @ServiceActivator(inputChannel = "pubsubInputChannel")
    // Binds this method to handle messages from 'pubsubInputChannel'.
    public MessageHandler receiveMessage() {
        return message -> {
            String payloadMessage = new String((byte[]) message.getPayload());
            System.out.printf("Message arrived! Payload: %s", payloadMessage);

            // Retrieves the original Pub/Sub message to acknowledge it after processing.
            BasicAcknowledgeablePubsubMessage originalMessage =
                    message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);

            receivedMessages.offer(payloadMessage); // Adds the message payload to a queue for consumption/testing.
            originalMessage.ack(); // Acknowledges the message to Pub/Sub, preventing redelivery.
        };
    }
}