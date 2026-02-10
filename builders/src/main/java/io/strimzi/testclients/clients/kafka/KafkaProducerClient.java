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
import io.strimzi.testclients.configuration.Transactional;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

@Buildable
public class KafkaProducerClient extends KafkaBaseClient {
    private String acks;
    private String message;
    private String messageTemplate;
    private String messageKey;
    private String headers;

    private String topicName;
    private Long delayMs;
    private Long messageCount;

    private Transactional transactional;

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
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

    public Transactional getTransactional() {
        return transactional;
    }

    public void setTransactional(Transactional transactional) {
        this.transactional = transactional;
    }

    public Job getJob() {
        List<EnvVar> producerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName(ConfigurationConstants.CLIENT_TYPE_ENV)
                .withValue(ClientType.KafkaProducer.name())
                .build()
        ));

        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.PRODUCER_ACKS_ENV, this.getAcks());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_ENV, this.getMessage());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_TEMPLATE_ENV, this.getMessageTemplate());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_KEY_ENV, this.getMessageKey());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.TOPIC_ENV, this.getTopicName());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.DELAY_MS_ENV, this.getDelayMs());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.MESSAGE_COUNT_ENV, this.getMessageCount());
        Environment.configureEnvVariableOrSkip(producerSpecificEnvVars, ConfigurationConstants.HEADERS_ENV, this.getHeaders());

        if (this.getTransactional() != null && this.getTransactional().getTransactionalEnvVar() != null) {
            producerSpecificEnvVars.add(this.getTransactional().getTransactionalEnvVar());
        }

        return getClientJob(producerSpecificEnvVars);
    }
}

