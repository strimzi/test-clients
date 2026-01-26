/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.http;

import io.strimzi.configuration.Tracing;
import io.strimzi.configuration.TracingBuilder;
import io.sundr.builder.annotations.Buildable;

@Buildable
public class HttpProducerConsumer extends HttpCommon {
    private String producerName;
    private String consumerName;

    private String message;
    private String consumerGroup;
    private String messageTemplate;
    private String clientId;
    private Long pollTimeout;

    private Long delayMs = 0L;

    private HttpProducerClient httpProducerClient;
    private HttpConsumerClient httpConsumerClient;

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        if (producerName == null) {
            throw new IllegalArgumentException("Producer name cannot be empty");
        }
        this.producerName = producerName;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        if (consumerName == null) {
            throw new IllegalArgumentException("Consumer name cannot be empty");
        }
        this.consumerName = consumerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(Long pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public Long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Long delayMs) {
        this.delayMs = delayMs;
    }

    public HttpProducerClient getProducer() {
        if (httpProducerClient == null) {
            httpProducerClient = configureProducer();
        }
        return httpProducerClient;
    }

    private HttpProducerClient configureProducer() {
        Tracing producerTracing = null;

        if (getTracing() != null) {
            producerTracing = new TracingBuilder()
                .withTracingType(getTracing().getTracingType())
                .withServiceNameEnvVar(getTracing().getServiceNameEnvVar())
                .withServiceName(getTracing().getServiceName() + "-producer")
                .build();
        }

        return new HttpProducerClientBuilder()
            .withName(getProducerName())
            .withNamespaceName(getNamespaceName())
            .withMessage(getMessage())
            .withMessageTemplate(getMessageTemplate())
            .withDelayMs(getDelayMs())
            .withHostname(getHostname())
            .withPort(getPort())
            .withMessageCount(getMessageCount())
            .withTopicName(getTopicName())
            .withImage(getImage())
            .withEndpointPrefix(getEndpointPrefix())
            .withSslTruststoreCertificate(getSslTruststoreCertificate())
            .withAdditionalEnvVars(getAdditionalEnvVars())
            .withMessageType(getMessageType())
            .withTracing(producerTracing)
            .build();
    }

    public HttpConsumerClient getConsumer() {
        if (httpConsumerClient == null) {
            httpConsumerClient = configureConsumer();
        }

        return httpConsumerClient;
    }

    private HttpConsumerClient configureConsumer() {
        Tracing consumerTracing = null;

        if (getTracing() != null) {
            consumerTracing = new TracingBuilder()
                .withTracingType(getTracing().getTracingType())
                .withServiceNameEnvVar(getTracing().getServiceNameEnvVar())
                .withServiceName(getTracing().getServiceName() + "-consumer")
                .build();
        }

        return new HttpConsumerClientBuilder()
            .withClientId(getClientId())
            .withPollTimeout(getPollTimeout())
            .withName(getConsumerName())
            .withNamespaceName(getNamespaceName())
            .withPollInterval(getDelayMs())
            .withConsumerGroup(getConsumerGroup())
            .withHostname(getHostname())
            .withPort(getPort())
            .withEndpointPrefix(getEndpointPrefix())
            .withMessageType(getMessageType())
            .withSslTruststoreCertificate(getSslTruststoreCertificate())
            .withTracing(consumerTracing)
            .withImage(getImage())
            .withAdditionalEnvVars(getAdditionalEnvVars())
            .withMessageCount(getMessageCount())
            .withTopicName(getTopicName())
            .build();
    }
}
