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
public class KafkaConsumerClient extends KafkaBaseClient {
    private String clientId;
    private String clientRack;
    private String topicName;
    private String consumerGroup;
    private Long delayMs;
    private Long messageCount;

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
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be empty");
        }
        this.topicName = topicName;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Long delayMs) {
        this.delayMs = delayMs;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Job getJob() {
        List<EnvVar> consumerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName(ConfigurationConstants.CLIENT_TYPE_ENV)
                .withValue(ClientType.KafkaConsumer.name())
                .build()
        ));

        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.CLIENT_ID_ENV, this.getClientId());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.CLIENT_RACK_ENV, this.getClientRack());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.TOPIC_ENV, this.getTopicName());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.GROUP_ID_ENV, this.getConsumerGroup());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.DELAY_MS_ENV, this.getDelayMs());
        Environment.configureEnvVariableOrSkip(consumerSpecificEnvVars, ConfigurationConstants.MESSAGE_COUNT_ENV, this.getMessageCount());

        return getClientJob(consumerSpecificEnvVars);
    }
}
