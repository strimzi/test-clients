/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.integration;

import io.strimzi.test.container.StrimziKafkaCluster;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.kafka.KafkaConsumerClient;
import io.strimzi.testclients.kafka.KafkaProducerClient;
import io.strimzi.testclients.testutils.TlsUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Runs the producer and consumer against a cluster that only exposes an mTLS listener,
 * with the certificates passed the way the clients get them in Kubernetes: PEM in CA_CRT, USER_CRT and USER_KEY.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaClientTlsIT {
    private static StrimziKafkaCluster kafkaCluster;
    private static String caCrt;
    private static String userCrt;
    private static String userKey;

    @BeforeAll
    static void setup() throws Exception {
        kafkaCluster = new StrimziKafkaCluster.StrimziKafkaClusterBuilder()
            .withNumberOfBrokers(1)
            .withInternalTopicReplicationFactor(1)
            .withSharedNetwork()
            .withTls()
            .build();
        kafkaCluster.start();

        caCrt = TlsUtils.caCertificate(kafkaCluster);
        userCrt = TlsUtils.userCertificateChain(kafkaCluster);
        userKey = TlsUtils.userKey(kafkaCluster);
    }

    @AfterAll
    static void afterAll() {
        kafkaCluster.stop();
    }

    @Test
    void testTlsExchange() throws Exception {
        String topicName = "my-tls-topic";

        // Topic is created by a plain Kafka Admin with the same PEM material, so the clients under test are the only TLS path we rely on
        try (Admin admin = Admin.create(TlsUtils.kafkaClientConfig(kafkaCluster))) {
            admin.createTopics(List.of(new NewTopic(topicName, 1, (short) 1))).all().get();
        }

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(tlsClientConfiguration(topicName));

        CompletableFuture<Void> future = CompletableFuture.runAsync(kafkaProducerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(30, TimeUnit.SECONDS);

        Field producedMessages = KafkaProducerClient.class.getDeclaredField("messageSuccessfullySent");
        producedMessages.setAccessible(true);
        assertThat(producedMessages.get(kafkaProducerClient), is(100));

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(tlsClientConfiguration(topicName));

        future = CompletableFuture.runAsync(kafkaConsumerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(30, TimeUnit.SECONDS);

        Field consumedMessages = KafkaConsumerClient.class.getDeclaredField("consumedMessages");
        consumedMessages.setAccessible(true);
        assertThat(consumedMessages.get(kafkaConsumerClient), is(100));
    }

    private Map<String, String> tlsClientConfiguration(String topicName) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");
        configuration.put(ConfigurationConstants.CA_CRT_ENV, caCrt);
        configuration.put(ConfigurationConstants.USER_CRT_ENV, userCrt);
        configuration.put(ConfigurationConstants.USER_KEY_ENV, userKey);
        return configuration;
    }
}
