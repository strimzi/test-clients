/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.http;

import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;

import java.util.Map;

public class HttpConsumerConfiguration extends HttpClientsConfiguration {
    private final String clientId;
    private final String groupId;
    private final long pollInterval;
    private final long pollTimeout;
    private final String consumerCreationURI;
    private final String subscriptionURI;
    private final String consumeMessagesURI;

    public HttpConsumerConfiguration(Map<String, String> map) {
        super(map);
        this.clientId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.CLIENT_ID_ENV), ConfigurationConstants.DEFAULT_CLIENT_ID);
        this.groupId = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.GROUP_ID_ENV), ConfigurationConstants.DEFAULT_GROUP_ID);
        this.pollInterval = ClientsConfigurationUtils.parseLongOrDefault(map.get(ConfigurationConstants.POLL_INTERVAL_ENV), ConfigurationConstants.DEFAULT_POLL_INTERVAL);
        this.pollTimeout = ClientsConfigurationUtils.parseLongOrDefault(map.get(ConfigurationConstants.POLL_TIMEOUT_ENV), ConfigurationConstants.DEFAULT_POLL_TIMEOUT);

        String baseUri = "http://" + this.getHostname() + ":" + this.getPort() + "/consumers/" + this.groupId;

        this.consumerCreationURI =  baseUri;
        this.subscriptionURI = baseUri + "/instances/" + this.clientId + "/subscription";
        this.consumeMessagesURI = baseUri + "/instances/" + this.clientId + "/records?timeout=" + this.pollTimeout;
    }

    public String getClientId() {
        return clientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public long getPollTimeout() {
        return pollTimeout;
    }

    public String getConsumerCreationURI() {
        return consumerCreationURI;
    }

    public String getSubscriptionURI() {
        return subscriptionURI;
    }

    public String getConsumeMessagesURI() {
        return consumeMessagesURI;
    }

    @Override
    public String toString() {
        return "HttpConsumerConfiguration:\n" +
            super.toString() + ",\n" +
            "clientId='" + this.getClientId() + "',\n" +
            "groupId='" + this.getGroupId() + "',\n" +
            "pollInterval='" + this.getPollInterval() + "',\n" +
            "pollTimeout='" + this.getPollTimeout() + "'";
    }
}
