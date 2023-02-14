/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

public interface Constants {
    long DEFAULT_MESSAGES_COUNT = 10;
    int DEFAULT_DELAY_MS = 0;
    String DEFAULT_MESSAGE = "Hello world";
    String DEFAULT_GROUP_ID = "my-group";
    String DEFAULT_CLIENT_ID = "my-consumer";
    int DEFAULT_POLL_INTERVAL = 1000;
    int DEFAULT_POLL_TIMEOUT = 100;

    /**
     * HTTP constants
     */
    String DEFAULT_ENDPOINT_PREFIX = "";

    String HOSTNAME_ENV = "HOSTNAME";
    String PORT_ENV = "PORT";
    String TOPIC_ENV = "TOPIC";
    String DELAY_MS_ENV = "DELAY_MS";
    String MESSAGE_COUNT_ENV = "MESSAGE_COUNT";
    String MESSAGE_ENV = "MESSAGE";
    String ENDPOINT_PREFIX_ENV = "ENDPOINT_PREFIX";
    String CLIENT_ID_ENV = "CLIENT_ID";
    String GROUP_ID_ENV = "GROUP_ID";
    String POLL_INTERVAL_ENV = "POLL_INTERVAL";
    String POLL_TIMEOUT_ENV = "POLL_TIMEOUT";

    /**
     * Common environment variables
     */
    String CLIENT_TYPE_ENV = "CLIENT_TYPE";

}
