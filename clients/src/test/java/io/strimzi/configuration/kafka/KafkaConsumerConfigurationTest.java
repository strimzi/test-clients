/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ConfigurationConstants;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

public class KafkaConsumerConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertThat(kafkaConsumerConfiguration.getClientRack(), nullValue());
        assertThat(kafkaConsumerConfiguration.getClientId(), is(ConfigurationConstants.DEFAULT_CLIENT_ID));
        assertThat(kafkaConsumerConfiguration.getGroupId(), is(ConfigurationConstants.DEFAULT_GROUP_ID));
        assertThat(kafkaConsumerConfiguration.getOutputFormat(), is(ConfigurationConstants.DEFAULT_OUTPUT_FORMAT));
    }

    @Test
    void testCustomConfiguration() {
        String clientId = "random-client-id";
        String groupId = "random-group-id";
        String clientRack = "rack-0";
        String outputFormat = "json";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
        configuration.put(ConfigurationConstants.CLIENT_ID_ENV, clientId);
        configuration.put(ConfigurationConstants.GROUP_ID_ENV, groupId);
        configuration.put(ConfigurationConstants.CLIENT_RACK_ENV, clientRack);
        configuration.put(ConfigurationConstants.OUTPUT_FORMAT_ENV, outputFormat);

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertAll("Checking custom configuration",
                () -> assertThat(kafkaConsumerConfiguration.getClientRack(), is(clientRack)),
                () -> assertThat(kafkaConsumerConfiguration.getClientId(), is(clientId)),
                () -> assertThat(kafkaConsumerConfiguration.getGroupId(), is(groupId)),
                () -> assertThat(kafkaConsumerConfiguration.getOutputFormat(), is(outputFormat))
        );
    }

    @Test
    void testInvalidConfiguration() {
        String topicName = "my-topic";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");

        assertThrows(InvalidParameterException.class, () -> new KafkaConsumerConfiguration(configuration));

        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertThat(kafkaConsumerConfiguration.getTopicName(), is(topicName));
    }
}
