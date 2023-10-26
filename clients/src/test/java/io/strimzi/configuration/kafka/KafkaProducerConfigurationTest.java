/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ConfigurationConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class KafkaProducerConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");

        KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        assertThat(kafkaProducerConfiguration.getAcks(), is(ConfigurationConstants.DEFAULT_PRODUCER_ACKS));
        assertThat(kafkaProducerConfiguration.getHeaders(), nullValue());
        assertThat(kafkaProducerConfiguration.getMessage(), is(ConfigurationConstants.DEFAULT_MESSAGE));
        assertThat(kafkaProducerConfiguration.getMessagesPerTransaction(), is(ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION));
        assertThat(kafkaProducerConfiguration.isTransactionalProducer(), is(false));
        assertThat(kafkaProducerConfiguration.getTopicName(), is("my-topic"));
    }

    @Test
    void testCustomConfiguration() {
        String bootstrapServer = "randomized-my-cluster-kafka:9092";
        String topicName = "epic-topic";
        String acks = "0";
        String headers = "header_key_one=header_value_one, header_key_two=header_value_two";
        String message = "Muhehe";
        int messagesPerTransaction = 125;
        String additionalConfig = ProducerConfig.TRANSACTIONAL_ID_CONFIG + " = my-id";

        List<Header> expectedHeadersList = new ArrayList<>();
        expectedHeadersList.add(new RecordHeader("header_key_one", "header_value_one".getBytes()));
        expectedHeadersList.add(new RecordHeader("header_key_two", "header_value_two".getBytes()));

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.PRODUCER_ACKS_ENV, acks);
        configuration.put(ConfigurationConstants.HEADERS_ENV, headers);
        configuration.put(ConfigurationConstants.MESSAGE_ENV, message);
        configuration.put(ConfigurationConstants.MESSAGES_PER_TRANSACTION_ENV, String.valueOf(messagesPerTransaction));
        configuration.put(ConfigurationConstants.ADDITIONAL_CONFIG_ENV, additionalConfig);

        KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        assertThat(kafkaProducerConfiguration.getAcks(), is(acks));
        assertThat(kafkaProducerConfiguration.getHeaders(), is(expectedHeadersList));
        assertThat(kafkaProducerConfiguration.getMessage(), is(message));
        assertThat(kafkaProducerConfiguration.getMessagesPerTransaction(), is(messagesPerTransaction));
        assertThat(kafkaProducerConfiguration.isTransactionalProducer(), is(true));
        assertThat(kafkaProducerConfiguration.getTopicName(), is(topicName));
        assertThat(kafkaProducerConfiguration.getBootstrapServers(), is(bootstrapServer));
    }

    @Test
    void testInvalidConfiguration() {
        int headers = 25;
        String messagesPerTransaction = "this will not work";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(ConfigurationConstants.MESSAGES_PER_TRANSACTION_ENV, messagesPerTransaction);

        assertThrows(InvalidParameterException.class, () -> new KafkaProducerConfiguration(configuration));

        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");

        KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        assertThat(kafkaProducerConfiguration.getMessagesPerTransaction(), is(ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION));

        configuration.put(ConfigurationConstants.HEADERS_ENV, String.valueOf(headers));

        assertThrows(RuntimeException.class, () -> new KafkaProducerConfiguration(configuration));
    }
}
