/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration.kafka;

import io.strimzi.testclients.configuration.ClientsConfigurationUtils;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseMapOfProperties;
import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseStringOrDefault;
import static io.strimzi.testclients.configuration.ConfigurationConstants.ADDITIONAL_CONFIG_ENV;


public class KafkaStreamsConfiguration extends KafkaClientsConfiguration {
    private final String applicationId;
    private final String sourceTopic;
    private final String targetTopic;
    private final long commitIntervalMs;
    private final String defaultKeySerde;
    private final String defaultValueSerde;

    public KafkaStreamsConfiguration(Map<String, String> map) {
        super(map);
        this.applicationId = map.get(ConfigurationConstants.APPLICATION_ID_ENV);
        this.sourceTopic = map.get(ConfigurationConstants.SOURCE_TOPIC_ENV);
        this.targetTopic = map.get(ConfigurationConstants.TARGET_TOPIC_ENV);
        this.commitIntervalMs = ClientsConfigurationUtils.parseLongOrDefault(map.get(ConfigurationConstants.COMMIT_INTERVAL_MS_ENV), ConfigurationConstants.DEFAULT_COMMIT_INTERVAL_MS);

        if (applicationId == null || applicationId.isEmpty()) throw new InvalidParameterException("Application ID is not set");

        if (sourceTopic == null || sourceTopic.isEmpty()) throw new InvalidParameterException("Source topic is not set");

        if (targetTopic == null || targetTopic.isEmpty()) throw new InvalidParameterException("Target topic is not set");

        Properties additionalConfig = parseMapOfProperties(parseStringOrDefault(map.get(ADDITIONAL_CONFIG_ENV), ""));
        if (additionalConfig.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG) != null) {
            this.defaultKeySerde = additionalConfig.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG).toString();
        } else {
            this.defaultKeySerde = Serdes.String().getClass().getName();
        }

        if (additionalConfig.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG) != null) {
            this.defaultValueSerde = additionalConfig.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG).toString();
        } else {
            this.defaultValueSerde = Serdes.String().getClass().getName();
        }
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public long getCommitIntervalMs() {
        return commitIntervalMs;
    }

    public String getDefaultKeySerde() {
        return defaultKeySerde;
    }

    public String getDefaultValueSerde() {
        return defaultValueSerde;
    }

    @Override
    public String toString() {
        return "KafkaStreamsConfiguration:\n" +
            super.toString() + ",\n" +
            "applicationId='" + this.getApplicationId() + "',\n" +
            "sourceTopic='" + this.getSourceTopic() + "',\n" +
            "targetTopic='" + this.getTargetTopic() + "',\n" +
            "commitIntervalMs='" + this.getCommitIntervalMs() + "'";
    }
}
