/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.unit.producer.kafka;

import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.kafka.KafkaProducerClient;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaProducerClientTest {

    private Map<String, String> configuration;

    @Test
    void testDefaultGenerateMessage() {
        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);
        List<ProducerRecord> producerRecords = kafkaProducerClient.generateMessages();

        assertAll(
                () -> assertThat(producerRecords.size(), is(ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION)),
                () -> assertThat("All keys shall be null", producerRecords.stream().allMatch(p -> p.key() == null)),
                () -> {
                    for (int i = 0; i < ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION; i++) {
                        assertThat(producerRecords.get(i).value(), is("Hello world - " + i));
                    }
                }
        );
    }

    @Test
    void testGenerateOneMessage() {
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "1");
        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);
        List<ProducerRecord> producerRecords = kafkaProducerClient.generateMessages();

        assertAll(
                () -> assertThat(producerRecords.size(), is(1)),
                () -> assertThat(producerRecords.get(0).key(), is(nullValue())),
                () -> assertThat(producerRecords.get(0).value(), is("Hello world - 0"))
        );
    }

    @Test
    void testGenerateMessageWithMessageKey() {
        String key = "my-key";
        configuration.put(ConfigurationConstants.MESSAGE_KEY_ENV, key);
        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);
        List<ProducerRecord> producerRecords = kafkaProducerClient.generateMessages();

        assertAll(
                () -> assertThat(producerRecords.size(), is(ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION)),
                () -> assertThat("All keys shall be the same", producerRecords.stream().allMatch(p -> p.key().equals(key)))
        );
    }

    @Test
    void testTopicEnvMandatory() {
        configuration.remove(ConfigurationConstants.TOPIC_ENV);
        String message = assertThrowsExactly(InvalidParameterException.class, () -> new KafkaProducerClient(configuration)).getMessage();
        assertThat(message, is("Topic is not set"));
    }

    @Test
    void testGenerateMessageWithTimestamp() {
        String timestamp = "2026-01-15T10:30:00Z";
        long timestampInMs = 1768473000000L;
        int messageCount = 10;
        long delayMs = 500L;

        Map<String, String> additionalConfig = new HashMap<>(configuration);
        additionalConfig.put(ConfigurationConstants.START_TIMESTAMP_ENV, timestamp);
        additionalConfig.put(ConfigurationConstants.DELAY_MS_ENV, "0");
        additionalConfig.put(ConfigurationConstants.MESSAGE_COUNT_ENV, String.valueOf(messageCount));

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(additionalConfig);
        List<ProducerRecord> producerRecords = kafkaProducerClient.generateMessages();

        // because DELAY_MS is set to 0, every image will have same timestamp
        producerRecords.forEach(producerRecord -> assertThat(producerRecord.timestamp(), is(timestampInMs)));

        // now configure DELAY_MS to 500ms, every message should have different timestamp
        additionalConfig.put(ConfigurationConstants.DELAY_MS_ENV, String.valueOf(delayMs));
        kafkaProducerClient = new KafkaProducerClient(additionalConfig);
        producerRecords = kafkaProducerClient.generateMessages();

        for (int messageIndex = 0; messageIndex < messageCount; messageIndex++) {
            long expectedTimestampMs = timestampInMs + (messageIndex * delayMs);
            assertThat(producerRecords.get(messageIndex).timestamp(), is(expectedTimestampMs));
        }
    }

    @BeforeEach
    void setup() {
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "localhost:8080");
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
    }
}
