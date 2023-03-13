/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.kafka;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static io.strimzi.common.configuration.Constants.APPLICATION_ID_ENV;
import static io.strimzi.common.configuration.Constants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.common.configuration.Constants.COMMIT_INTERVAL_MS_ENV;
import static io.strimzi.common.configuration.Constants.DEFAULT_COMMIT_INTERVAL_MS;
import static io.strimzi.common.configuration.Constants.SOURCE_TOPIC_ENV;
import static io.strimzi.common.configuration.Constants.TARGET_TOPIC_ENV;
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
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(APPLICATION_ID_ENV, appId);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(SOURCE_TOPIC_ENV, sourceTopic);

        assertThrows(InvalidParameterException.class, () -> new KafkaStreamsConfiguration(configuration));

        configuration.put(TARGET_TOPIC_ENV, targetTopic);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        assertThat(kafkaStreamsConfiguration.getApplicationId(), is(appId));
        assertThat(kafkaStreamsConfiguration.getSourceTopic(), is(sourceTopic));
        assertThat(kafkaStreamsConfiguration.getTargetTopic(), is(targetTopic));
        assertThat(kafkaStreamsConfiguration.getCommitIntervalMs(), is(DEFAULT_COMMIT_INTERVAL_MS));
    }

    @Test
    void testCustomConfiguration() {
        String appId = "absolute-id";
        String sourceTopic = "second-cluster-kafka:9093";
        String targetTopic = "arnost-topic";
        String bootstrapServer = "alice-topic";
        long commitIntervalMs = 800000L;

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(SOURCE_TOPIC_ENV, sourceTopic);
        configuration.put(TARGET_TOPIC_ENV, targetTopic);
        configuration.put(APPLICATION_ID_ENV, appId);
        configuration.put(COMMIT_INTERVAL_MS_ENV, String.valueOf(commitIntervalMs));

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
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(SOURCE_TOPIC_ENV, sourceTopic);
        configuration.put(TARGET_TOPIC_ENV, targetTopic);
        configuration.put(APPLICATION_ID_ENV, appId);
        configuration.put(COMMIT_INTERVAL_MS_ENV, commitIntervalMs);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        assertThat(kafkaStreamsConfiguration.getCommitIntervalMs(), is(DEFAULT_COMMIT_INTERVAL_MS));
    }
}
