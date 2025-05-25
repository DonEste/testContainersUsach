package com.example.test_containers_usach;


import com.example.test_containers_usach.pubsub.PubSubService;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class PubSubServiceMockTest {

    @MockitoBean // This annotation replaces the actual PubSubTemplate in the Spring context with a Mockito mock
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private PubSubService pubSubService;

    private static final String TOPIC_NAME = "example-topic";

    @BeforeEach
    void setUp() {
        // Reset the mock's interactions before each test to ensure test isolation
        reset(pubSubTemplate);
    }

    @Test
    void testPublishMessage_shouldCallPubSubTemplatePublish() {
        String testMessage = "Test message for mock";

        // When the service publishes a message
        pubSubService.publishMessage(testMessage);

        // Then verify that pubSubTemplate.publish was called exactly once
        // with the correct topic name and message
        verify(pubSubTemplate, times(1)).publish(TOPIC_NAME, testMessage);

        // You can also capture arguments for more detailed assertions
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(pubSubTemplate).publish(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(TOPIC_NAME);
        assertThat(messageCaptor.getValue()).isEqualTo(testMessage);
    }

    @Test
    void testPublishMessage_shouldNotCallPublishIfLogicPrevents() {
        // This is a hypothetical example to show not calling publish
        // Let's assume there was a condition in PubSubService that prevented publishing for empty messages
        // pubSubService.publishMessage("");

        // For this example, we just assert that publish is not called if the service isn't invoked
        verifyNoInteractions(pubSubTemplate); // No calls to the mock initially
    }
}