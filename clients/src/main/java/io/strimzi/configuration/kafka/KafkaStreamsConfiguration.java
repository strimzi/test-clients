/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;

import java.security.InvalidParameterException;
import java.util.Map;


public class KafkaStreamsConfiguration extends KafkaClientsConfiguration {
    private final String applicationId;
    private final String sourceTopic;
    private final String targetTopic;
    private final long commitIntervalMs;

    public KafkaStreamsConfiguration(Map<String, String> map) {
        super(map);
        this.applicationId = map.get(ConfigurationConstants.APPLICATION_ID_ENV);
        this.sourceTopic = map.get(ConfigurationConstants.SOURCE_TOPIC_ENV);
        this.targetTopic = map.get(ConfigurationConstants.TARGET_TOPIC_ENV);
        this.commitIntervalMs = ClientsConfigurationUtils.parseLongOrDefault(map.get(ConfigurationConstants.COMMIT_INTERVAL_MS_ENV), ConfigurationConstants.DEFAULT_COMMIT_INTERVAL_MS);

        if (applicationId == null || applicationId.isEmpty()) throw new InvalidParameterException("Application ID is not set");

        if (sourceTopic == null || sourceTopic.isEmpty()) throw new InvalidParameterException("Source topic is not set");

        if (targetTopic == null || targetTopic.isEmpty()) throw new InvalidParameterException("Target topic is not set");
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