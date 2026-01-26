/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration.http;

import io.strimzi.common.MessageType;
import io.strimzi.configuration.ClientsConfigurationUtils;
import io.strimzi.configuration.ConfigurationConstants;

import java.security.InvalidParameterException;
import java.util.Map;

public class HttpClientsConfiguration {
    private final String hostname;
    private final String port;
    private final String topic;
    private final long delay;
    private final int messageCount;
    private final String endpointPrefix;
    private final String messageType;
    private final String sslTruststoreCertificate;
    private final String urlPrefix;

    public HttpClientsConfiguration(Map<String, String> map) {
        String hostname = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.HOSTNAME_ENV), "");
        String port = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.PORT_ENV), "");
        String topic = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.TOPIC_ENV), "");
        long delay = ClientsConfigurationUtils.parseLongOrDefault(map.get(ConfigurationConstants.DELAY_MS_ENV), ConfigurationConstants.DEFAULT_DELAY_MS);
        int messageCount = ClientsConfigurationUtils.parseIntOrDefault(map.get(ConfigurationConstants.MESSAGE_COUNT_ENV), ConfigurationConstants.DEFAULT_MESSAGES_COUNT);
        String endpointPrefix = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), ConfigurationConstants.DEFAULT_ENDPOINT_PREFIX);
        this.messageType = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.MESSAGE_TYPE_ENV), ConfigurationConstants.DEFAULT_MESSAGE_TYPE);
        this.sslTruststoreCertificate = ClientsConfigurationUtils.parseStringOrDefault(map.get(ConfigurationConstants.CA_CRT_ENV), null);
        this.urlPrefix = sslTruststoreCertificate == null ? "http://" : "https://";

        if (MessageType.getFromString(this.messageType) == MessageType.UNKNOWN) {
            throw new InvalidParameterException("MESSAGE_TYPE should be one of " + MessageType.supportedTypes());
        }

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

    public String getMessageType() {
        return messageType;
    }

    public String getSslTruststoreCertificate() {
        return sslTruststoreCertificate;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    @Override
    public String toString() {
        return "hostname='" + this.getHostname() + "',\n" +
            "port='" + this.getPort() + "',\n" +
            "topic='" + this.getTopic() + "',\n" +
            "delay='" + this.getDelay() + "',\n" +
            "messageType='" + this.getMessageType() + "',\n" +
            "messageCount='" + this.getMessageCount() + "'";
    }
}
