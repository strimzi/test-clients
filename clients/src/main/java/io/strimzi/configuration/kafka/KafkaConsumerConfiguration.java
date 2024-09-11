/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.security.InvalidParameterException;
import java.util.Map;


public class KafkaConsumerConfiguration extends KafkaClientsConfiguration {
    private final String groupId;
    private final String clientId;
    private final String clientRack;
    private final String topicName;
    private final String outputFormat;
    private final String keyDeserializer;
    private final String valueDeserializer;

    public KafkaConsumerConfiguration(Map<String, String> map) {
        super(map);
        this.groupId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.GROUP_ID_ENV), ConfigurationConstants.DEFAULT_GROUP_ID);
        this.clientId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.CLIENT_ID_ENV), ConfigurationConstants.DEFAULT_CLIENT_ID);
        this.clientRack = map.get(ConfigurationConstants.CLIENT_RACK_ENV);
        this.topicName = map.get(ConfigurationConstants.TOPIC_ENV);
        this.outputFormat = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.OUTPUT_FORMAT_ENV), ConfigurationConstants.DEFAULT_OUTPUT_FORMAT);

        if (this.topicName == null || topicName.isEmpty()) throw new InvalidParameterException("Topic is not set");
        this.keyDeserializer = map.getOrDefault(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.valueDeserializer = map.getOrDefault(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    }

    public String getGroupId() {
        return groupId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientRack() {
        return clientRack;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getKeyDeserializer() {
        return keyDeserializer;
    }

    public String getValueDeserializer() {
        return valueDeserializer;
    }

    @Override
    public String toString() {
        return "KafkaConsumerConfiguration:\n" +
            super.toString() + ",\n" +
            "groupId='" + this.getGroupId() + "',\n" +
            "clientId='" + this.getClientId() + "',\n" +
            "clientRack='" + this.getClientRack() + "',\n" +
            "topicName='" + this.getTopicName() + "',\n" +
            "outputFormat='" + this.getOutputFormat() + "'";
    }
}
