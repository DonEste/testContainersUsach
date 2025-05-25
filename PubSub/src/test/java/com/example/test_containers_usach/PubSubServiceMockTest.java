package com.example.test_containers_usach;

import com.example.PubSubService;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PubSubService}, isolating it from actual Google Cloud Pub/Sub
 * by using Mockito to mock its dependencies.
 */
@SpringBootTest // Boots a Spring application context for testing.
@ContextConfiguration(classes = PubSubServiceMockTest.TestConfig.class) // Uses a minimal test configuration.
// Excludes auto-configurations related to GCP Pub/Sub to prevent real connections.
@EnableAutoConfiguration(exclude = {
        GcpPubSubAutoConfiguration.class, // Prevents auto-configuration of Pub/Sub client.
        GcpContextAutoConfiguration.class, // Prevents general GCP context auto-configuration.
})
class PubSubServiceMockTest {

    private static final String TOPIC_NAME = "example-topic"; // The expected topic name used by the service.

    @MockitoBean // Replaces the real PubSubTemplate bean with a Mockito mock.
    private PubSubTemplate pubSubTemplate;

    @MockitoBean // Mocks the subscriber template; it's not directly used in PubSubService but might be in other tests.
    private PubSubSubscriberTemplate pubSubSubscriberTemplate;

    @Autowired // Injects the PubSubService into the test class; it will receive the mocked dependencies.
    private PubSubService pubSubService;

    /**
     * Resets mocks before each test to ensure a clean state and prevent test interference.
     */
    @BeforeEach
    void setUp() {
        reset(pubSubTemplate, pubSubSubscriberTemplate); // Clears previous interactions and stubbings.
    }

    /**
     * Verifies that no publish operations are called on the mocked PubSubTemplate by default.
     * This acts as a sanity check.
     */
    @Test
    void testPublishMessage_shouldNotCallPublishIfLogicPrevents() {
        verifyNoInteractions(pubSubTemplate); // Asserts no methods were called on pubSubTemplate initially.
        verifyNoInteractions(pubSubSubscriberTemplate); // Asserts no methods were called on pubSubSubscriberTemplate initially.
    }

    /**
     * Tests the core message publishing functionality: verifies the service transforms the message
     * and calls the `pubSubTemplate.publish` method with the correct arguments.
     */
    @Test
    void testPubSubMessageFlow() {
        String message = "test message";
        String expectedPublishedMessage = message.toUpperCase(); // Expects message to be uppercased by service.

        pubSubService.publishMessage(message); // Call the service method.

        // Verifies that the publish method was called exactly once with the expected topic and transformed message.
        verify(pubSubTemplate, times(1)).publish(TOPIC_NAME, expectedPublishedMessage);
        verifyNoMoreInteractions(pubSubTemplate); // Ensures no other methods were called on the template.
    }

    /**
     * Defines the minimal Spring context for this unit test.
     * It only imports `PubSubService` as its dependencies (`PubSubTemplate`) are mocked.
     */
    @Configuration
    @Import({PubSubService.class}) // Imports the service under test.
    static class TestConfig {
        // No additional beans are defined here; mocks handle the dependencies.
    }
}