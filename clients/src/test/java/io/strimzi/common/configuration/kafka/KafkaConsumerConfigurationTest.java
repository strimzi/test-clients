/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.kafka;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static io.strimzi.common.configuration.Constants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.common.configuration.Constants.CLIENT_ID_ENV;
import static io.strimzi.common.configuration.Constants.CLIENT_RACK_ENV;
import static io.strimzi.common.configuration.Constants.DEFAULT_CLIENT_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_GROUP_ID;
import static io.strimzi.common.configuration.Constants.GROUP_ID_ENV;
import static io.strimzi.common.configuration.Constants.TOPIC_ENV;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class KafkaConsumerConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertThat(kafkaConsumerConfiguration.getClientRack(), nullValue());
        assertThat(kafkaConsumerConfiguration.getClientId(), is(DEFAULT_CLIENT_ID));
        assertThat(kafkaConsumerConfiguration.getGroupId(), is(DEFAULT_GROUP_ID));
    }

    @Test
    void testCustomConfiguration() {
        String clientId = "random-client-id";
        String groupId = "random-group-id";
        String clientRack = "rack-0";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");
        configuration.put(CLIENT_ID_ENV, clientId);
        configuration.put(GROUP_ID_ENV, groupId);
        configuration.put(CLIENT_RACK_ENV, clientRack);

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertThat(kafkaConsumerConfiguration.getClientRack(), is(clientRack));
        assertThat(kafkaConsumerConfiguration.getClientId(), is(clientId));
        assertThat(kafkaConsumerConfiguration.getGroupId(), is(groupId));
    }

    @Test
    void testInvalidConfiguration() {
        String topicName = "my-topic";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");

        assertThrows(InvalidParameterException.class, () -> new KafkaConsumerConfiguration(configuration));

        configuration.put(TOPIC_ENV, topicName);

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        assertThat(kafkaConsumerConfiguration.getTopicName(), is(topicName));
    }
}
