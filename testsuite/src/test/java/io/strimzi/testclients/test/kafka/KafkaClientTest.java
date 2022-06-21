/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.kafka;

import io.strimzi.test.container.StrimziKafkaContainer;
import io.strimzi.testclients.kafka.admin.KafkaAdminApp;
import io.strimzi.testclients.kafka.admin.AdminConfiguration;
import io.strimzi.testclients.kafka.consumer.ConsumerConfiguration;
import io.strimzi.testclients.kafka.consumer.KafkaConsumerApp;
import io.strimzi.testclients.kafka.producer.KafkaProducerApp;
import io.strimzi.testclients.kafka.producer.ProducerConfiguration;
import io.strimzi.testclients.kafka.streams.KafkaStreamsApp;
import io.strimzi.testclients.kafka.streams.StreamsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@Testcontainers
public class KafkaClientTest {

    private static final String TOPIC = "mytopic";
    private static final String INPUT_TOPIC = TOPIC + "-0";
    private static final String OUTPUT_TOPIC = TOPIC + "-1";

    @Container
    StrimziKafkaContainer kafkaContainer = new StrimziKafkaContainer();

    @BeforeEach
    public void ensureTopics() {
        AdminConfiguration adminConfiguration = new AdminConfiguration() {
            @Override
            public String getBootstrapServers() {
                return kafkaContainer.getBootstrapServers();
            }

            @Override
            public String getTopicOperation() {
                return "create";
            }

            @Override
            public String getTopic() {
                return TOPIC;
            }

            @Override
            public String getNumberOfTopics() {
                return "2";
            }

            @Override
            public String getCompact() {
                return "false";
            }
        };
        KafkaAdminApp.start(adminConfiguration);
    }

    @Test
    public void testProduceStreamsConsume() throws Exception {
        ProducerConfiguration producerConfiguration = new ProducerConfiguration() {
            @Override
            public String getBootstrapServers() {
                return kafkaContainer.getBootstrapServers();
            }

            @Override
            public String getTopic() {
                return INPUT_TOPIC;
            }
        };
        KafkaProducerApp.start(producerConfiguration, false);

        StreamsConfiguration streamsConfiguration = new StreamsConfiguration() {
            @Override
            public String getBootstrapServers() {
                return kafkaContainer.getBootstrapServers();
            }

            @Override
            public String getApplicationId() {
                return "testclients";
            }

            @Override
            public String getSourceTopic() {
                return INPUT_TOPIC;
            }

            @Override
            public String getTargetTopic() {
                return OUTPUT_TOPIC;
            }
        };
        KafkaStreamsApp.start(streamsConfiguration);

        ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration() {
            @Override
            public String getBootstrapServers() {
                return kafkaContainer.getBootstrapServers();
            }

            @Override
            public String getTopic() {
                return OUTPUT_TOPIC;
            }

            @Override
            public String getGroupId() {
                return UUID.randomUUID().toString();
            }
        };
        KafkaConsumerApp.start(consumerConfiguration);
    }

}
