package com.example.test_containers_usach;

import com.example.test_containers_usach.pubsub.PubSubListener;
import com.example.test_containers_usach.pubsub.PubSubService;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = PubSubIntegrationTest.PubSubEmulatorInitializer.class)
class PubSubIntegrationTest {

    // Define the Pub/Sub emulator container
    @Container
    public static PubSubEmulatorContainer pubsubEmulator =
            new PubSubEmulatorContainer(DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:388.0.0-emulators"));

    @Autowired
    private PubSubService pubSubService;

    @Autowired
    private PubSubListener pubSubListener;

    private static final String PROJECT_ID = "test-project";
    private static final String TOPIC_NAME = "example-topic";
    private static final String SUBSCRIPTION_NAME = "example-subscription";

    private static TopicAdminClient topicAdminClient;
    private static SubscriptionAdminClient subscriptionAdminClient;

    @BeforeAll
    static void setupPubSubEmulator() throws IOException {
        // Create a channel to connect to the emulator

        ManagedChannel channel =
                ManagedChannelBuilder.forTarget("dns:///" + pubsubEmulator.getEmulatorEndpoint())
                        .usePlaintext()
                        .build();

        FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

        // Create TopicAdminClient and SubscriptionAdminClient for setup
        topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build());


        subscriptionAdminClient = SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build());


        // Ensure the topic and subscription exist in the emulator
        ProjectTopicName topicProjectName = ProjectTopicName.of(PROJECT_ID, TOPIC_NAME);

        TopicAdminClient.ListTopicsPagedResponse listTopicsPagedResponse = topicAdminClient.listTopics(ProjectName.of(PROJECT_ID));

        if (StreamSupport.stream(topicAdminClient.listTopics(ProjectName.of(PROJECT_ID)).iterateAll().spliterator(), false)
                .noneMatch(topic -> topic.getName().equals(topicProjectName.toString()))) {
            topicAdminClient.createTopic(topicProjectName);
        }

        ProjectSubscriptionName subscriptionProjectName = ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_NAME);
        if (StreamSupport.stream(subscriptionAdminClient.listSubscriptions(ProjectName.of(PROJECT_ID)).iterateAll().spliterator(), false)
                .noneMatch(sub -> sub.getName().equals(subscriptionProjectName.toString()))) {
            subscriptionAdminClient.createSubscription(subscriptionProjectName, topicProjectName, PushConfig.getDefaultInstance(), 0);
        }
    }

    @AfterAll
    static void cleanupPubSubEmulator() {
        if (topicAdminClient != null) {
            topicAdminClient.close();
        }
        if (subscriptionAdminClient != null) {
            subscriptionAdminClient.close();
        }
    }

    @BeforeEach
    void clearMessages() {
        pubSubListener.getReceivedMessages().clear(); // Clear messages before each test
    }

    @Test
    void testPubSubMessageFlow() {
        String testMessage = "Hello Testcontainers Pub/Sub!";

        // Publish a message
        pubSubService.publishMessage(testMessage);

        // Await the message being received by the listener
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !pubSubListener.getReceivedMessages().isEmpty());

        // Assert that the correct message was received
        assertThat(pubSubListener.getReceivedMessages()).containsExactly(testMessage);
    }

    /**
     * This initializer dynamically sets Spring Boot properties to connect to the Pub/Sub emulator.
     */
    static class PubSubEmulatorInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.cloud.gcp.pubsub.emulator-host=" + pubsubEmulator.getEmulatorEndpoint(),
                    "spring.cloud.gcp.pubsub.project-id=" + PROJECT_ID,
                    "spring.cloud.gcp.pubsub.credentials.location=classpath:test-credentials.json" // Placeholder for test credentials
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}