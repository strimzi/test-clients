/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Header;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

public class KafkaProducerConfiguration extends KafkaClientsConfiguration {

    private final String acks;
    private final List<Header> headers;
    private final int messagesPerTransaction;
    private final String topicName;
    private final boolean transactionalProducer;
    private final String message;
    public KafkaProducerConfiguration(Map<String, String> map) {
        super(map);
        this.acks = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.PRODUCER_ACKS_ENV), ConfigurationConstants.DEFAULT_PRODUCER_ACKS);
        this.headers = ClientsConfigurationUtils.parseHeadersFromConfiguration(ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.HEADERS_ENV), null));
        this.messagesPerTransaction = ClientsConfigurationUtils.parseIntOrDefault(map.get(ConfigurationConstants.MESSAGES_PER_TRANSACTION_ENV), ConfigurationConstants.DEFAULT_MESSAGES_PER_TRANSACTION);
        this.transactionalProducer = getAdditionalConfig().toString().contains(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
        this.message = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.MESSAGE_ENV), ConfigurationConstants.DEFAULT_MESSAGE);
        this.topicName = map.get(ConfigurationConstants.TOPIC_ENV);

        if (this.topicName == null || topicName.isEmpty()) throw new InvalidParameterException("Topic is not set");
    }

    public String getAcks() {
        return acks;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public int getMessagesPerTransaction() {
        return messagesPerTransaction;
    }

    public String getTopicName() {
        return topicName;
    }

    public boolean isTransactionalProducer() {
        return transactionalProducer;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "KafkaProducerConfiguration:\n" +
            super.toString() + ",\n" +
            "acks='" + this.getAcks() + "',\n" +
            "headers='" + this.getHeaders() + "',\n" +
            "topicName='" + this.getTopicName() + "',\n" +
            "messagesPerTransaction='" + this.getMessagesPerTransaction() + "',\n" +
            "transactionalProducer='" + this.isTransactionalProducer() + "',\n" +
            "message='" + this.getMessage() + "'";
    }
}
