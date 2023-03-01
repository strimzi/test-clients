/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import java.util.Map;

import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseLongOrDefault;
import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseStringOrDefault;
import static io.strimzi.common.configuration.Constants.CLIENT_ID_ENV;
import static io.strimzi.common.configuration.Constants.DEFAULT_CLIENT_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_GROUP_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_POLL_INTERVAL;
import static io.strimzi.common.configuration.Constants.DEFAULT_POLL_TIMEOUT;
import static io.strimzi.common.configuration.Constants.GROUP_ID_ENV;
import static io.strimzi.common.configuration.Constants.POLL_INTERVAL_ENV;
import static io.strimzi.common.configuration.Constants.POLL_TIMEOUT_ENV;

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
        this.clientId = parseStringOrDefault(map.get(CLIENT_ID_ENV), DEFAULT_CLIENT_ID);
        this.groupId = parseStringOrDefault(map.get(GROUP_ID_ENV), DEFAULT_GROUP_ID);
        this.pollInterval = parseLongOrDefault(map.get(POLL_INTERVAL_ENV), DEFAULT_POLL_INTERVAL);
        this.pollTimeout = parseLongOrDefault(map.get(POLL_TIMEOUT_ENV), DEFAULT_POLL_TIMEOUT);

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
