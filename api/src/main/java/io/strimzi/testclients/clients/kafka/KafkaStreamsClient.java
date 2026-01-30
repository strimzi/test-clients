/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.testclients.configuration.ClientType;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.Environment;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

@Buildable
public class KafkaStreamsClient extends KafkaBaseClient {
    private String applicationId;
    private String sourceTopicName;
    private String targetTopicName;
    private Long commitIntervalMs;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        if (applicationId == null || applicationId.isEmpty()) {
            throw new IllegalArgumentException("Application ID cannot be empty");
        }
        this.applicationId = applicationId;
    }

    public String getSourceTopicName() {
        return sourceTopicName;
    }

    public void setSourceTopicName(String sourceTopicName) {
        if (sourceTopicName == null || sourceTopicName.isEmpty()) {
            throw new IllegalArgumentException("Source topic name cannot be empty");
        }
        this.sourceTopicName = sourceTopicName;
    }

    public String getTargetTopicName() {
        return targetTopicName;
    }

    public void setTargetTopicName(String targetTopicName) {
        if (targetTopicName == null || targetTopicName.isEmpty()) {
            throw new IllegalArgumentException("Target topic name cannot be empty");
        }
        this.targetTopicName = targetTopicName;
    }

    public Long getCommitIntervalMs() {
        return commitIntervalMs;
    }

    public void setCommitIntervalMs(Long commitIntervalMs) {
        this.commitIntervalMs = commitIntervalMs;
    }

    public Job getJob() {
        List<EnvVar> streamsSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName(ConfigurationConstants.CLIENT_TYPE_ENV)
                .withValue(ClientType.KafkaStreams.name())
                .build()
        ));

        Environment.configureEnvVariableOrSkip(streamsSpecificEnvVars, ConfigurationConstants.APPLICATION_ID_ENV, this.getApplicationId());
        Environment.configureEnvVariableOrSkip(streamsSpecificEnvVars, ConfigurationConstants.SOURCE_TOPIC_ENV, this.getSourceTopicName());
        Environment.configureEnvVariableOrSkip(streamsSpecificEnvVars, ConfigurationConstants.TARGET_TOPIC_ENV, this.getTargetTopicName());
        Environment.configureEnvVariableOrSkip(streamsSpecificEnvVars, ConfigurationConstants.COMMIT_INTERVAL_MS_ENV, this.getCommitIntervalMs());


        return getClientJob(streamsSpecificEnvVars);
    }
}
