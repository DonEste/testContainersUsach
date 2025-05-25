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

import org.springframework.test.context.bean.override.mockito.MockitoBean; // Specific import for MockitoBean

import static org.mockito.Mockito.*; // Static imports for Mockito methods

/**
 * Unit tests for {@link PubSubService} using Mockito.
 * This class isolates the service from actual Google Cloud Pub/Sub connections.
 */
@SpringBootTest // Loads a Spring Boot context for testing.
// Configures the test context to use only TestConfig, avoiding full app scan.
@ContextConfiguration(classes = PubSubServiceMockTest.TestConfig.class)
// Excludes GCP Pub/Sub auto-configurations to prevent real connection attempts.
@EnableAutoConfiguration(exclude = {
        GcpPubSubAutoConfiguration.class, // Prevents Pub/Sub client setup.
        GcpContextAutoConfiguration.class, // Prevents core GCP context (e.g., project ID).
})
class PubSubServiceMockTest {

    // The expected Pub/Sub topic name.
    private static final String TOPIC_NAME = "example-topic";

    // Mocks the PubSubTemplate to control its behavior and verify calls.
    @MockitoBean
    private PubSubTemplate pubSubTemplate;

    // Mocks the PubSubSubscriberTemplate to avoid real subscriber setup.
    @MockitoBean
    private PubSubSubscriberTemplate pubSubSubscriberTemplate;

    // Injects the actual PubSubService instance; it will receive the mocks.
    @Autowired
    private PubSubService pubSubService;

    /**
     * Sets up a clean state before each test.
     */
    @BeforeEach
    void setUp() {
        // Resets all mocks to clear previous interactions/stubbings.
        reset(pubSubTemplate, pubSubSubscriberTemplate);
    }

    /**
     * Verifies that PubSubService doesn't call publish methods if its internal logic prevents it.
     */
    @Test
    void testPublishMessage_shouldNotCallPublishIfLogicPrevents() {
        // Confirms no methods were called on the mocked PubSubTemplate.
        verifyNoInteractions(pubSubTemplate);
        // Confirms no methods were called on the mocked PubSubSubscriberTemplate.
        verifyNoInteractions(pubSubSubscriberTemplate);
    }

    /**
     * Tests the message publishing process: service calls template, message is transformed.
     */
    @Test
    void testPubSubMessageFlow() {
        String message = "test message"; // Input message.
        String expectedPublishedMessage = message.toUpperCase(); // Expected message after transformation.

        // Calls the service method to be tested.
        pubSubService.publishMessage(message);

        // Verifies 'publish' was called exactly once on the mock, with precise arguments.
        verify(pubSubTemplate, times(1)).publish(TOPIC_NAME, expectedPublishedMessage);
        // Ensures no other unexpected calls were made to the mock.
        verifyNoMoreInteractions(pubSubTemplate);
    }

    /**
     * Defines the minimal Spring context for this test class.
     */
    @Configuration
    // Imports PubSubService into the test context. Its dependencies (the mocks) will be fulfilled.
    @Import({PubSubService.class})
    static class TestConfig {
        // No additional configurations needed here.
    }
}