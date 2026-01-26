/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.configuration.Transactional;
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
        this.topicName = topicName;
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

    public Transactional getTransactional() {
        return transactional;
    }

    public void setTransactional(Transactional transactional) {
        this.transactional = transactional;
    }

    public Job getProducer() {
        List<EnvVar> producerSpecificEnvVars = new ArrayList<>(List.of(
            new EnvVarBuilder()
                .withName("PRODUCER_ACKS")
                .withValue(this.getAcks())
                .build(),
            new EnvVarBuilder()
                .withName("MESSAGE")
                .withValue(this.getMessage())
                .build(),
            new EnvVarBuilder()
                .withName("MESSAGE_TEMPLATE")
                .withValue(this.getMessageTemplate())
                .build(),
            new EnvVarBuilder()
                .withName("MESSAGE_KEY")
                .withValue(this.getMessageKey())
                .build(),
            new EnvVarBuilder()
                .withName("TOPIC")
                .withValue(this.getTopicName())
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
                .withValue("KafkaProducer")
                .build()
        ));

        if (this.getHeaders() != null && !this.getHeaders().isEmpty()) {
            producerSpecificEnvVars.add(new EnvVarBuilder()
                .withName("HEADERS")
                .withValue(this.getHeaders())
                .build()
            );
        }

        if (this.getTransactional().getTransactionalEnvVar() != null) {
            producerSpecificEnvVars.add(this.getTransactional().getTransactionalEnvVar());
        }

        return getClientJob(producerSpecificEnvVars);
    }
}

