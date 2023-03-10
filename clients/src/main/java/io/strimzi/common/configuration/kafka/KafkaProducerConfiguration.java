/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Header;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseHeadersFromConfiguration;
import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseIntOrDefault;
import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseStringOrDefault;
import static io.strimzi.common.configuration.Constants.DEFAULT_MESSAGE;
import static io.strimzi.common.configuration.Constants.DEFAULT_MESSAGES_PER_TRANSACTION;
import static io.strimzi.common.configuration.Constants.DEFAULT_PRODUCER_ACKS;
import static io.strimzi.common.configuration.Constants.HEADERS_ENV;
import static io.strimzi.common.configuration.Constants.MESSAGES_PER_TRANSACTION_ENV;
import static io.strimzi.common.configuration.Constants.MESSAGE_ENV;
import static io.strimzi.common.configuration.Constants.PRODUCER_ACKS_ENV;
import static io.strimzi.common.configuration.Constants.TOPIC_ENV;

public class KafkaProducerConfiguration extends KafkaClientsConfiguration {

    private final String acks;
    private final List<Header> headers;
    private final int messagesPerTransaction;
    private final String topicName;
    private final boolean transactionalProducer;
    private final String message;
    public KafkaProducerConfiguration(Map<String, String> map) {
        super(map);
        this.acks = parseStringOrDefault(map.get(PRODUCER_ACKS_ENV), DEFAULT_PRODUCER_ACKS);
        this.headers = parseHeadersFromConfiguration(parseStringOrDefault(map.get(HEADERS_ENV), null));
        this.messagesPerTransaction = parseIntOrDefault(map.get(MESSAGES_PER_TRANSACTION_ENV), DEFAULT_MESSAGES_PER_TRANSACTION);
        this.transactionalProducer = getAdditionalConfig().toString().contains(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
        this.message = parseStringOrDefault(map.get(MESSAGE_ENV), DEFAULT_MESSAGE);
        this.topicName = map.get(TOPIC_ENV);

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
            "message='" + this.getMessage() + "'";
    }
}
