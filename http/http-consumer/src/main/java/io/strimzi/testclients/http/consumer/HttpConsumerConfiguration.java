/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.http.consumer;

import java.security.InvalidParameterException;

public class HttpConsumerConfiguration {

    private static final String DEFAULT_GROUP_ID = "my-group";
    private static final String DEFAULT_CLIENT_ID = "my-consumer";
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_PORT = "PORT";
    private static final String ENV_TOPIC = "TOPIC";
    private static final String ENV_CLIENT_ID = "CLIENT_ID";
    private static final String ENV_GROUP_ID = "GROUP_ID";
    private static final String ENV_POLL_INTERVAL = "POLL_INTERVAL";
    private static final String ENV_POLL_TIMEOUT = "POLL_TIMEOUT";
    private static final String ENV_MESSAGE_COUNT = "MESSAGE_COUNT";
    private static final String ENV_ENDPOINT_PREFIX = "ENDPOINT_PREFIX";

    private final String hostname;
    private final int port;
    private final String topic;
    private final String clientId;
    private final String groupId;
    private final int pollInterval;
    private final int pollTimeout;
    private final Long messageCount;
    private final String endpointPrefix;

    public HttpConsumerConfiguration() {
        this(
            System.getenv(ENV_HOSTNAME),
            System.getenv(ENV_PORT),
            System.getenv(ENV_TOPIC)
        );
    }

    public HttpConsumerConfiguration(String hostname, String port, String topic) {
        if (hostname == null || hostname.isEmpty()) throw new InvalidParameterException("Hostname is not set.");
        this.hostname = hostname;

        if (port == null || port.isEmpty()) throw new InvalidParameterException("Port is not set.");
        this.port = Integer.parseInt(port);

        if (topic == null || topic.isEmpty()) throw new InvalidParameterException("Topic is not set.");
        this.topic = topic;

        this.clientId = System.getenv(ENV_CLIENT_ID) == null ? DEFAULT_CLIENT_ID : System.getenv(ENV_CLIENT_ID);
        this.pollInterval = System.getenv(ENV_POLL_INTERVAL) == null ? DEFAULT_POLL_INTERVAL : Integer.parseInt(System.getenv(ENV_POLL_INTERVAL));
        this.pollTimeout = System.getenv(ENV_POLL_TIMEOUT) == null ? DEFAULT_POLL_TIMEOUT : Integer.parseInt(System.getenv(ENV_POLL_TIMEOUT));
        this.messageCount = System.getenv(ENV_MESSAGE_COUNT) == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv(ENV_MESSAGE_COUNT));
        this.endpointPrefix = System.getenv(ENV_ENDPOINT_PREFIX) == null ? DEFAULT_ENDPOINT_PREFIX : System.getenv(ENV_ENDPOINT_PREFIX);
        this.groupId = System.getenv(ENV_GROUP_ID) == null ? DEFAULT_GROUP_ID : System.getenv(ENV_GROUP_ID);
    }

    /**
     * @return hostname to which connect to
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return host port to which connect to
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Kafka topic from which consume messages
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return Kafka consumer clientId used as consumer name
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return consumer group name the consumer belong to
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return interval (in ms) for polling to get messages
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * @return timeout (in ms) for polling to get messages
     */
    public int getPollTimeout() {
        return pollTimeout;
    }

    /**
     * @return number of messages to receive
     */
    public Long getMessageCount() {
        return messageCount;
    }

    /**
     * @return a prefix to use in the endpoint path
     */
    public String getEndpointPrefix() {
        return endpointPrefix;
    }

    @Override
    public String toString() {
        return "HttpKafkaConsumerConfig(" +
            "hostname=" + getHostname() +
            ",port=" + getPort() +
            ",topic=" + getTopic() +
            ",clientId=" + getClientId() +
            ",groupid=" + getGroupId() +
            ",pollInterval=" + getPollInterval() +
            ",pollTimeout=" + getPollTimeout() +
            ",messageCount=" + getMessageCount() +
            ",endpointPrefix=" + getEndpointPrefix() +
            ")";
    }
}
