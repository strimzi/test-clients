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
public class KafkaConsumerClient extends KafkaBaseClient {
    private String clientId;
    private String clientRack;
    private String topicName;
    private String consumerGroup;
    private long delayMs;
    private long messageCount;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientRack() {
        return clientRack;
    }

    public void setClientRack(String clientRack) {
        this.clientRack = clientRack;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public Job getConsumer() {
        List<EnvVar> consumerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName("CLIENT_ID")
                .withValue(this.getClientId())
                .build(),
            new EnvVarBuilder()
                .withName("CLIENT_RACK")
                .withValue(this.getClientRack())
                .build(),
            new EnvVarBuilder()
                .withName("TOPIC")
                .withValue(this.getTopicName())
                .build(),
            new EnvVarBuilder()
                .withName("GROUP_ID")
                .withValue(this.getConsumerGroup())
                .build(),
            new EnvVarBuilder()
                .withName("DELAY_MS")
                .withValue(String.valueOf(this.getDelayMs()))
                .build(),
            new EnvVarBuilder()
                .withName("MESSAGE_COUNT")
                .withValue(String.valueOf(this.getMessageCount()))
                .build(),
            new EnvVarBuilder()
                .withName("CLIENT_TYPE")
                .withValue("KafkaConsumer")
                .build()
        ));

        return getClientJob(consumerSpecificEnvVars);
    }
}
