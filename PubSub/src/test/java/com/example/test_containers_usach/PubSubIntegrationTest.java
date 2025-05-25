package com.example.test_containers_usach;

import com.example.PubSubListener;
import com.example.PubSubService;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
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
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@ContextConfiguration(initializers = PubSubIntegrationTest.PubSubEmulatorInitializer.class)
class PubSubIntegrationTest {

    private static final String PROJECT_ID = "test-project";
    private static final String TOPIC_NAME = "example-topic";
    private static final String SUBSCRIPTION_NAME = "example-subscription";

    @Container
    public static PubSubEmulatorContainer pubsubEmulator =
            new PubSubEmulatorContainer(DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:388.0.0-emulators"));

    private static TopicAdminClient topicAdminClient;
    private static SubscriptionAdminClient subscriptionAdminClient;

    @Autowired
    private PubSubService pubSubService;

    @Autowired
    private PubSubListener pubSubListener;

    @BeforeAll
    static void setupPubSubEmulator() throws IOException {
        ManagedChannel channel =
                ManagedChannelBuilder.forTarget("dns:///" + pubsubEmulator.getEmulatorEndpoint())
                        .usePlaintext()
                        .build();

        FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

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

        ProjectTopicName topicProjectName = ProjectTopicName.of(PROJECT_ID, TOPIC_NAME);

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
        pubSubListener.getReceivedMessages().clear();
    }

    @Test
    void testPubSubMessageFlow() {
        String testMessage = "Hello Testcontainers Pub/Sub!";

        pubSubService.publishMessage(testMessage);

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !pubSubListener.getReceivedMessages().isEmpty());

        assertThat(pubSubListener.getReceivedMessages()).containsExactly(testMessage.toUpperCase());
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
                    "spring.cloud.gcp.pubsub.credentials.location=classpath:secrets.json" // Placeholder for test credentials
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}