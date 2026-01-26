/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
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
        this.applicationId = applicationId;
    }

    public String getSourceTopicName() {
        return sourceTopicName;
    }

    public void setSourceTopicName(String sourceTopicName) {
        this.sourceTopicName = sourceTopicName;
    }

    public String getTargetTopicName() {
        return targetTopicName;
    }

    public void setTargetTopicName(String targetTopicName) {
        this.targetTopicName = targetTopicName;
    }

    public Long getCommitIntervalMs() {
        return commitIntervalMs;
    }

    public void setCommitIntervalMs(Long commitIntervalMs) {
        this.commitIntervalMs = commitIntervalMs;
    }

    public Job getStreams() {
        List<EnvVar> streamsSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName("APPLICATION_ID")
                .withValue(this.getApplicationId())
                .build(),
            new EnvVarBuilder()
                .withName("SOURCE_TOPIC")
                .withValue(this.getSourceTopicName())
                .build(),
            new EnvVarBuilder()
                .withName("TARGET_TOPIC")
                .withValue(this.getTargetTopicName())
                .build(),
            new EnvVarBuilder()
                .withName("COMMIT_INTERVAL_MS")
                .withValue(String.valueOf(this.getCommitIntervalMs()))
                .build(),
            new EnvVarBuilder()
                .withName("CLIENT_TYPE")
                .withValue("KafkaStreams")
                .build()
        ));

        return getClientJob(streamsSpecificEnvVars);
    }
}
