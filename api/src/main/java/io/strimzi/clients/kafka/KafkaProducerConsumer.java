/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.strimzi.configuration.Transactional;
import io.sundr.builder.annotations.Buildable;

@Buildable
public class KafkaProducerConsumer extends KafkaCommon {
    private String producerName;
    private String consumerName;

    private String topicName;
    private String clientId;
    private String clientRack;
    private String consumerGroup;
    private String acks;
    private String message;
    private String messageTemplate;
    private String messageKey;
    private String headers;

    private Long messageCount;
    private Long delayMs;

    private Transactional transactional;

    private KafkaProducerClient kafkaProducerClient;
    private KafkaConsumerClient kafkaConsumerClient;

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

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

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getAcks() {
        return acks;
    }

    public Transactional getTransactional() {
        return transactional;
    }

    public void setTransactional(Transactional transactional) {
        this.transactional = transactional;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAcks(String acks) {
        this.acks = acks;
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

    public KafkaProducerClient getProducer() {
        if (kafkaProducerClient == null) {
            kafkaProducerClient = configureProducer();
        }

        return kafkaProducerClient;
    }

    private KafkaProducerClient configureProducer() {
        return new KafkaProducerClientBuilder()
            .withAcks(getAcks())
            .withMessage(getMessage())
            .withMessageTemplate(getMessageTemplate())
            .withMessageKey(getMessageKey())
            .withHeaders(getHeaders())
            .withTopicName(getTopicName())
            .withTransactional(getTransactional())
            .withName(getProducerName())
            .withNamespaceName(getNamespaceName())
            .withBootstrapAddress(getBootstrapAddress())
            .withImage(getImage())
            .withOauth(getOauth())
            .withSasl(getSasl())
            .withSsl(getSsl())
            .withTracing(getTracing())
            .withAdditionalEnvVars(getAdditionalEnvVars())
            .withAdditionalConfig(getAdditionalConfig())
            .build();


    }

    public KafkaConsumerClient getConsumer() {
        if (kafkaConsumerClient == null) {
            kafkaConsumerClient = configureConsumer();
        }

        return kafkaConsumerClient;
    }

    private KafkaConsumerClient configureConsumer() {
        return new KafkaConsumerClientBuilder()
            .withClientId(getClientId())
            .withClientRack(getClientRack())
            .withTopicName(getTopicName())
            .withConsumerGroup(getConsumerGroup())
            .withDelayMs(getDelayMs())
            .withMessageCount(getMessageCount())
            .withName(getConsumerName())
            .withNamespaceName(getNamespaceName())
            .withBootstrapAddress(getBootstrapAddress())
            .withImage(getImage())
            .withOauth(getOauth())
            .withSasl(getSasl())
            .withSsl(getSsl())
            .withTracing(getTracing())
            .withAdditionalEnvVars(getAdditionalEnvVars())
            .withAdditionalConfig(getAdditionalConfig())
            .build();

    }
}
