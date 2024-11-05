/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.integration;

import io.strimzi.test.container.StrimziKafkaCluster;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractIT {
    protected static StrimziKafkaCluster kafkaCluster;

    @BeforeAll
    static void setup() {
        kafkaCluster = new StrimziKafkaCluster.StrimziKafkaClusterBuilder()
            .withKraft()
            .withNumberOfBrokers(1)
            .withInternalTopicReplicationFactor(1)
            .withSharedNetwork()
            .build();
        kafkaCluster.start();
    }

    @AfterAll
    static void afterAll() {
        kafkaCluster.stop();
    }

    protected void createKafkaTopic(String name, Map<String, String> config) throws ExecutionException, InterruptedException {
        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster.getBootstrapServers()))) {

            NewTopic topic = new NewTopic(name, 1, (short) 1);
            topic.configs(config);

            admin.createTopics(List.of(topic)).all().get();
        }
    }
}
