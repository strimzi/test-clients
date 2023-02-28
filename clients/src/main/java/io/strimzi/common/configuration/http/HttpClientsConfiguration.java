/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import java.security.InvalidParameterException;
import java.util.Map;

import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseIntOrDefault;
import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseLongOrDefault;
import static io.strimzi.common.configuration.ClientsConfigurationUtils.parseStringOrDefault;
import static io.strimzi.common.configuration.Constants.DEFAULT_DELAY_MS;
import static io.strimzi.common.configuration.Constants.DEFAULT_ENDPOINT_PREFIX;
import static io.strimzi.common.configuration.Constants.DEFAULT_MESSAGES_COUNT;
import static io.strimzi.common.configuration.Constants.DELAY_MS_ENV;
import static io.strimzi.common.configuration.Constants.ENDPOINT_PREFIX_ENV;
import static io.strimzi.common.configuration.Constants.HOSTNAME_ENV;
import static io.strimzi.common.configuration.Constants.MESSAGE_COUNT_ENV;
import static io.strimzi.common.configuration.Constants.PORT_ENV;
import static io.strimzi.common.configuration.Constants.TOPIC_ENV;

public class HttpClientsConfiguration {
    private final String hostname;
    private final String port;
    private final String topic;
    private final long delay;
    private final int messageCount;
    private final String endpointPrefix;

    public HttpClientsConfiguration(Map<String, String> map) {
        String hostname = parseStringOrDefault(map.get(HOSTNAME_ENV), "");
        String port = parseStringOrDefault(map.get(PORT_ENV), "");
        String topic = parseStringOrDefault(map.get(TOPIC_ENV), "");
        long delay = parseLongOrDefault(map.get(DELAY_MS_ENV), DEFAULT_DELAY_MS);
        int messageCount = parseIntOrDefault(map.get(MESSAGE_COUNT_ENV), DEFAULT_MESSAGES_COUNT);
        String endpointPrefix = parseStringOrDefault(map.get(ENDPOINT_PREFIX_ENV), DEFAULT_ENDPOINT_PREFIX);

        if (hostname == null || hostname.isEmpty()) throw new InvalidParameterException("Hostname is not set.");

        if (port == null || port.isEmpty()) throw new InvalidParameterException("Port is not set.");

        if (topic == null || topic.isEmpty()) throw new InvalidParameterException("Topic is not set.");

        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.delay = delay;
        this.messageCount = messageCount;
        this.endpointPrefix = endpointPrefix;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getTopic() {
        return topic;
    }

    public long getDelay() {
        return delay;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getEndpointPrefix() {
        return endpointPrefix;
    }

    @Override
    public String toString() {
        return "hostname='" + this.getHostname() + "',\n" +
            "port='" + this.getPort() + "',\n" +
            "topic='" + this.getTopic() + "',\n" +
            "delay='" + this.getDelay() + "',\n" +
            "messageCount='" + this.getMessageCount() + "'";
    }
}
