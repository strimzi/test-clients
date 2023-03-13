/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.kafka;

import java.security.InvalidParameterException;
import java.util.Map;

import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseStringOrDefault;
import static io.strimzi.common.configuration.Constants.CLIENT_ID_ENV;
import static io.strimzi.common.configuration.Constants.CLIENT_RACK_ENV;
import static io.strimzi.common.configuration.Constants.DEFAULT_CLIENT_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_GROUP_ID;
import static io.strimzi.common.configuration.Constants.GROUP_ID_ENV;
import static io.strimzi.common.configuration.Constants.TOPIC_ENV;

public class KafkaConsumerConfiguration extends KafkaClientsConfiguration {
    private final String groupId;
    private final String clientId;
    private final String clientRack;
    private final String topicName;

    public KafkaConsumerConfiguration(Map<String, String> map) {
        super(map);
        this.groupId = parseStringOrDefault(map.get(GROUP_ID_ENV), DEFAULT_GROUP_ID);
        this.clientId = parseStringOrDefault(map.get(CLIENT_ID_ENV), DEFAULT_CLIENT_ID);
        this.clientRack = map.get(CLIENT_RACK_ENV);
        this.topicName = map.get(TOPIC_ENV);

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
