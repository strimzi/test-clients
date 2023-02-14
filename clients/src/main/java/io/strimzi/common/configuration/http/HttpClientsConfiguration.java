/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import java.security.InvalidParameterException;
import java.util.Map;

import static io.strimzi.common.configuration.ClientsConfigurationUtils.*;
import static io.strimzi.common.configuration.Constants.*;

public class HttpClientsConfiguration {
    private final String hostname;
    private final String port;
    private final String topic;
    private final int delay;
    private final Long messageCount;
    private final String endpointPrefix;

    public HttpClientsConfiguration(Map<String, String> map) {
        String hostname = parseStringOrDefault(map.get(HOSTNAME_ENV), "");
        String port = parseStringOrDefault(map.get(PORT_ENV), "");
        String topic = parseStringOrDefault(map.get(TOPIC_ENV), "");
        int delay = parseIntOrDefault(map.get(DELAY_MS_ENV), DEFAULT_DELAY_MS);
        Long messageCount = parseLongOrDefault(map.get(MESSAGE_COUNT_ENV), DEFAULT_MESSAGES_COUNT);
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

    public int getDelay() {
        return delay;
    }

    public Long getMessageCount() {
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
