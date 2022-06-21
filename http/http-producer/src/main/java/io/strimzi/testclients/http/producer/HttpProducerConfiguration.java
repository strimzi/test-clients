/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.http.producer;

import java.security.InvalidParameterException;

public class HttpProducerConfiguration {

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final int DEFAULT_DELAY_MS = 0;
    private static final String DEFAULT_MESSAGE = "Hello world";
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_PORT = "PORT";
    private static final String ENV_TOPIC = "TOPIC";
    private static final String ENV_DELAY_MS = "DELAY_MS";
    private static final String ENV_MESSAGE_COUNT = "MESSAGE_COUNT";
    private static final String ENV_MESSAGE = "MESSAGE";
    private static final String ENV_ENDPOINT_PREFIX = "ENDPOINT_PREFIX";

    private final String hostname;
    private final int port;
    private final String topic;
    private final int delay;
    private final Long messageCount;
    private final String message;
    private final String endpointPrefix;
    private final String uri;

    public HttpProducerConfiguration() {
        this(
            System.getenv(ENV_HOSTNAME),
            System.getenv(ENV_PORT),
            System.getenv(ENV_TOPIC)
        );
    }

    public HttpProducerConfiguration(String hostname, String port, String topic) {
        if (hostname == null || hostname.isEmpty()) throw new InvalidParameterException("Hostname is not set.");
        this.hostname = hostname;

        if (port == null || port.isEmpty()) throw new InvalidParameterException("Port is not set.");
        this.port = Integer.parseInt(port);

        if (topic == null || topic.isEmpty()) throw new InvalidParameterException("Topic is not set.");
        this.topic = topic;
        this.delay = System.getenv(ENV_DELAY_MS) == null ? DEFAULT_DELAY_MS : Integer.parseInt(System.getenv(ENV_DELAY_MS));
        this.messageCount = System.getenv(ENV_MESSAGE_COUNT) == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv(ENV_MESSAGE_COUNT));
        this.message = System.getenv(ENV_MESSAGE) == null ? DEFAULT_MESSAGE : System.getenv(ENV_MESSAGE);
        this.endpointPrefix = System.getenv(ENV_ENDPOINT_PREFIX) == null ? DEFAULT_ENDPOINT_PREFIX : System.getenv(ENV_ENDPOINT_PREFIX);

        this.uri = "http://" + this.hostname + ":" + this.port + this.endpointPrefix + "/topics/" + this.topic;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
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

    public String getMessage() {
        return message;
    }

    public String getEndpointPrefix() {
        return endpointPrefix;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "HttpProducerConfig{" +
            "hostname='" + getHostname() + '\'' +
            ", port='" + getPort() + '\'' +
            ", topic='" + getTopic() + '\'' +
            ", delay=" + getDelay() +
            ", messageCount=" + getMessageCount() +
            ", message='" + getMessage() + '\'' +
            '}';
    }
}
