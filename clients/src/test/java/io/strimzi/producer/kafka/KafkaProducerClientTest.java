/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.producer.kafka;

import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.kafka.KafkaProducerClient;
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

    @BeforeEach
    void setup() {
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "localhost:8080");
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
    }
}
