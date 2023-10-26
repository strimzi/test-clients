/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.kafka;

import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;

import java.security.InvalidParameterException;
import java.util.Map;


public class KafkaConsumerConfiguration extends KafkaClientsConfiguration {
    private final String groupId;
    private final String clientId;
    private final String clientRack;
    private final String topicName;

    public KafkaConsumerConfiguration(Map<String, String> map) {
        super(map);
        this.groupId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.GROUP_ID_ENV), ConfigurationConstants.DEFAULT_GROUP_ID);
        this.clientId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.CLIENT_ID_ENV), ConfigurationConstants.DEFAULT_CLIENT_ID);
        this.clientRack = map.get(ConfigurationConstants.CLIENT_RACK_ENV);
        this.topicName = map.get(ConfigurationConstants.TOPIC_ENV);

        if (this.topicName == null || topicName.isEmpty()) throw new InvalidParameterException("Topic is not set");
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

    @Override
    public String toString() {
        return "KafkaConsumerConfiguration:\n" +
            super.toString() + ",\n" +
            "groupId='" + this.getGroupId() + "',\n" +
            "clientId='" + this.getClientId() + "',\n" +
            "clientRack='" + this.getClientRack() + "',\n" +
            "topicName='" + this.getTopicName() + "'";
    }
}
