/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.unit.configuration.kafka;

import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.kafka.KafkaStreamsConfiguration;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KafkaStreamsConfigurationTest {
    @Test
    void testDefaultConfiguration() {
        // by default all Streams config have to be set
        String bootstrapServer = "target-my-topic";
        String sourceTopic = "my-cluster-kafka:9092";
        String targetTopic = "source-my-topic";
        String appId = "my-app-0";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(ConfigurationConstants.APPLICATION_ID_ENV, appId);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(ConfigurationConstants.SOURCE_TOPIC_ENV, sourceTopic);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(ConfigurationConstants.TARGET_TOPIC_ENV, targetTopic);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        assertThat(kafkaStreamsConfiguration.getApplicationId(), is(appId));
        assertThat(kafkaStreamsConfiguration.getSourceTopic(), is(sourceTopic));
        assertThat(kafkaStreamsConfiguration.getTargetTopic(), is(targetTopic));
        assertThat(kafkaStreamsConfiguration.getCommitIntervalMs(), is(ConfigurationConstants.DEFAULT_COMMIT_INTERVAL_MS));
    }

    @Test
    void testCustomConfiguration() {
        String appId = "absolute-id";
        String sourceTopic = "second-cluster-kafka:9093";
        String targetTopic = "arnost-topic";
        String bootstrapServer = "alice-topic";
        long commitIntervalMs = 800000L;

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(ConfigurationConstants.SOURCE_TOPIC_ENV, sourceTopic);
        configuration.put(ConfigurationConstants.TARGET_TOPIC_ENV, targetTopic);
        configuration.put(ConfigurationConstants.APPLICATION_ID_ENV, appId);
        configuration.put(ConfigurationConstants.COMMIT_INTERVAL_MS_ENV, String.valueOf(commitIntervalMs));

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        assertThat(kafkaStreamsConfiguration.getApplicationId(), is(appId));
        assertThat(kafkaStreamsConfiguration.getSourceTopic(), is(sourceTopic));
        assertThat(kafkaStreamsConfiguration.getTargetTopic(), is(targetTopic));
        assertThat(kafkaStreamsConfiguration.getCommitIntervalMs(), is(commitIntervalMs));
    }

    @Test
    void testInvalidConfiguration() {
        String appId = "absolute-id";
        String sourceTopic = "second-cluster-kafka:9093";
        String targetTopic = "arnost-topic";
        String bootstrapServer = "alice-topic";
        String commitIntervalMs = "this is only thing which is wrong";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(ConfigurationConstants.SOURCE_TOPIC_ENV, sourceTopic);
        configuration.put(ConfigurationConstants.TARGET_TOPIC_ENV, targetTopic);
        configuration.put(ConfigurationConstants.APPLICATION_ID_ENV, appId);
        configuration.put(ConfigurationConstants.COMMIT_INTERVAL_MS_ENV, commitIntervalMs);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        assertThat(kafkaStreamsConfiguration.getCommitIntervalMs(), is(ConfigurationConstants.DEFAULT_COMMIT_INTERVAL_MS));
    }
}
