/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.integration;

import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.kafka.KafkaConsumerClient;
import io.strimzi.kafka.KafkaProducerClient;
import io.strimzi.test.container.StrimziKafkaCluster;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaClientIT {
    private static StrimziKafkaCluster kafkaCluster;

    @BeforeEach
    void setup() {
        kafkaCluster = new StrimziKafkaCluster.StrimziKafkaClusterBuilder()
            .withKraft()
            .withNumberOfBrokers(1)
            .withInternalTopicReplicationFactor(1)
            .withSharedNetwork()
            .build();
        kafkaCluster.start();
    }

    @AfterAll
    public static void afterAll() {
        kafkaCluster.stop();
    }

    private void createKafkaTopic(String name, Map<String, String> config) throws ExecutionException, InterruptedException {
        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster.getBootstrapServers()))) {

            NewTopic topic = new NewTopic(name, 1, (short) 1);
            topic.configs(config);

            admin.createTopics(List.of(topic)).all().get();
        }
    }

    @Test
    void testSimpleExchange() throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, TimeoutException {
        Map<String, String> configuration;
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");

        createKafkaTopic("my-topic", Map.of(TopicConfig.RETENTION_MS_CONFIG, "604800000"));

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);

        CompletableFuture<Void> future = CompletableFuture.runAsync(kafkaProducerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field producedMessages = KafkaProducerClient.class.getDeclaredField("messageSuccessfullySent");
        producedMessages.setAccessible(true);
        assertThat(producedMessages.get(kafkaProducerClient), is(100));

        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(configuration);

        future = CompletableFuture.runAsync(kafkaConsumerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field consumedMessages = KafkaConsumerClient.class.getDeclaredField("consumedMessages");
        consumedMessages.setAccessible(true);
        assertThat(consumedMessages.get(kafkaConsumerClient), is(100));
    }
}
