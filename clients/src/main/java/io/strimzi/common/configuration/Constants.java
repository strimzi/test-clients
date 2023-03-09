/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

public interface Constants {
    int DEFAULT_MESSAGES_COUNT = 10;
    long DEFAULT_DELAY_MS = 0;

    String DEFAULT_MESSAGE = "Hello world";
    String DEFAULT_GROUP_ID = "my-group";
    String DEFAULT_CLIENT_ID = "my-consumer";
    long DEFAULT_POLL_INTERVAL = 1000;
    long DEFAULT_POLL_TIMEOUT = 100;
    long DEFAULT_TASK_COMPLETION_TIMEOUT = 60000;
    String DEFAULT_PRODUCER_ACKS = "1";
    long DEFAULT_COMMIT_INTERVAL_MS = 5000;
    int DEFAULT_MESSAGES_PER_TRANSACTION = 10;

    /**
     * HTTP constants
     */
    String DEFAULT_ENDPOINT_PREFIX = "";

    /**
     * HTTP environment variables
     */
    String HOSTNAME_ENV = "HOSTNAME";
    String PORT_ENV = "PORT";
    String ENDPOINT_PREFIX_ENV = "ENDPOINT_PREFIX";
    String CLIENT_ID_ENV = "CLIENT_ID";
    String POLL_INTERVAL_ENV = "POLL_INTERVAL";
    String POLL_TIMEOUT_ENV = "POLL_TIMEOUT";

    /**
     * Common environment variables
     */
    String GROUP_ID_ENV = "GROUP_ID";
    String TOPIC_ENV = "TOPIC";
    String MESSAGE_COUNT_ENV = "MESSAGE_COUNT";
    String DELAY_MS_ENV = "DELAY_MS";
    String MESSAGE_ENV = "MESSAGE";

    /**
     * Kafka environment variables
     */
    String BOOTSTRAP_SERVERS_ENV = "BOOTSTRAP_SERVERS";
    String APPLICATION_ID_ENV = "APPLICATION_ID";
    String SOURCE_TOPIC_ENV = "SOURCE_TOPIC";
    String TARGET_TOPIC_ENV = "TARGET_TOPIC";
    String SASL_JAAS_CONFIG_ENV = "SASL_JAAS_CONFIG";
    String USER_NAME_ENV = "USER_NAME";
    String USER_PASSWORD_ENV = "USER_PASSWORD";
    String SASL_MECHANISM_ENV = "SASL_MECHANISM_ENV";
    String COMMIT_INTERVAL_MS_ENV = "COMMIT_INTERVAL_MS";
    String PRODUCER_ACKS_ENV = "PRODUCER_ACKS";
    String CLIENT_RACK_ENV = "CLIENT_RACK";
    String CA_CRT_ENV = "CA_CRT";
    String USER_KEY_ENV = "USER_KEY";
    String USER_CERT_ENV = "USER_CERT";
    String OAUTH_CLIENT_ID_ENV = "OAUTH_CLIENT_ID";
    String OAUTH_CLIENT_SECRET_ENV = "OAUTH_CLIENT_SECRET";
    String OAUTH_ACCESS_TOKEN_ENV = "OAUTH_ACCESS_TOKEN";
    String OAUTH_REFRESH_TOKEN_ENV = "OAUTH_REFRESH_TOKEN";
    String OAUTH_TOKEN_ENDPOINT_URI_ENV = "OAUTH_TOKEN_ENDPOINT_URI";
    String HEADERS_ENV = "HEADERS";
    String MESSAGES_PER_TRANSACTION_ENV = "MESSAGES_PER_TRANSACTION";
    String ADDITIONAL_CONFIG_ENV = "ADDITIONAL_CONFIG";

    /**
     * Common environment variables
     */
    String CLIENT_TYPE_ENV = "CLIENT_TYPE";

    String HTTP_JSON_CONTENT_TYPE = "application/vnd.kafka.json.v2+json";
}
