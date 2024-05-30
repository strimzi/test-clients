/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

import java.util.List;

public interface ConfigurationConstants {
    int DEFAULT_MESSAGES_COUNT = 10;
    long DEFAULT_DELAY_MS = 0;

    String DEFAULT_MESSAGE = "Hello world";
    String DEFAULT_GROUP_ID = "my-group";
    String DEFAULT_CLIENT_ID = "my-consumer";
    String DEFAULT_OUTPUT_FORMAT = "plain";
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
     * Producer environment variables
     */
    String MESSAGE_KEY_ENV = "MESSAGE_KEY";

    /**
     * Consumer environment variables
     */
    String OUTPUT_FORMAT_ENV = "OUTPUT_FORMAT";

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
    String APPLICATION_ID_ENV = "APPLICATION_ID";
    String SOURCE_TOPIC_ENV = "SOURCE_TOPIC";
    String TARGET_TOPIC_ENV = "TARGET_TOPIC";
    String COMMIT_INTERVAL_MS_ENV = "COMMIT_INTERVAL_MS";
    String PRODUCER_ACKS_ENV = "PRODUCER_ACKS";
    String CLIENT_RACK_ENV = "CLIENT_RACK";
    String HEADERS_ENV = "HEADERS";
    String MESSAGES_PER_TRANSACTION_ENV = "MESSAGES_PER_TRANSACTION";
    /**
     * Kafka basic env variables
     */
    String BOOTSTRAP_SERVERS_ENV = "BOOTSTRAP_SERVERS";
    String SASL_JAAS_CONFIG_ENV = "SASL_JAAS_CONFIG";
    String USER_NAME_ENV = "USER_NAME";
    String USER_PASSWORD_ENV = "USER_PASSWORD";
    String SASL_MECHANISM_ENV = "SASL_MECHANISM_ENV";
    String CA_CRT_ENV = "CA_CRT";
    String USER_KEY_ENV = "USER_KEY";
    String USER_CRT_ENV = "USER_CRT";
    String OAUTH_CLIENT_ID_ENV = "OAUTH_CLIENT_ID";
    String OAUTH_CLIENT_SECRET_ENV = "OAUTH_CLIENT_SECRET";
    String OAUTH_ACCESS_TOKEN_ENV = "OAUTH_ACCESS_TOKEN";
    String OAUTH_REFRESH_TOKEN_ENV = "OAUTH_REFRESH_TOKEN";
    String OAUTH_TOKEN_ENDPOINT_URI_ENV = "OAUTH_TOKEN_ENDPOINT_URI";
    String ADDITIONAL_CONFIG_ENV = "ADDITIONAL_CONFIG";

    List<String> BASIC_ENV_LIST = List.of(
        BOOTSTRAP_SERVERS_ENV,
        SASL_JAAS_CONFIG_ENV,
        USER_NAME_ENV,
        USER_PASSWORD_ENV,
        SASL_MECHANISM_ENV,
        CA_CRT_ENV,
        USER_KEY_ENV,
        USER_CRT_ENV,
        OAUTH_CLIENT_ID_ENV,
        OAUTH_CLIENT_SECRET_ENV,
        OAUTH_ACCESS_TOKEN_ENV,
        OAUTH_TOKEN_ENDPOINT_URI_ENV,
        ADDITIONAL_CONFIG_ENV
    );

    /**
     * Kafka basic variables in properties format
     */
    String BOOTSTRAP_SERVERS_PROPERTY = "bootstrap.servers";
    String SASL_JAAS_CONFIG_PROPERTY = "sasl.jaas.config";
    String USER_NAME_PROPERTY = "user.name";
    String USER_PASSWORD_PROPERTY = "user.password";
    String SASL_MECHANISM_PROPERTY = "sasl.mechanism";
    String CA_CRT_PROPERTY = "ca.crt";
    String USER_KEY_PROPERTY = "user.key";
    String USER_CRT_PROPERTY = "user.crt";
    String OAUTH_CLIENT_ID_PROPERTY = "oauth.client.id";
    String OAUTH_CLIENT_SECRET_PROPERTY = "oauth.client.secret";
    String OAUTH_ACCESS_TOKEN_PROPERTY = "oauth.access.token";
    String OAUTH_REFRESH_TOKEN_PROPERTY = "oauth.refresh.token";
    String OAUTH_TOKEN_ENDPOINT_URI_PROPERTY = "oauth.token.endpoint.uri";
    String ADDITIONAL_CONFIG_PROPERTY = "additional.config";

    List<String> BASIC_PROPERTY_LIST = List.of(
        BOOTSTRAP_SERVERS_PROPERTY,
        SASL_JAAS_CONFIG_PROPERTY,
        USER_NAME_PROPERTY,
        USER_PASSWORD_PROPERTY,
        SASL_MECHANISM_PROPERTY,
        CA_CRT_PROPERTY,
        USER_KEY_PROPERTY,
        USER_CRT_PROPERTY,
        OAUTH_CLIENT_ID_PROPERTY,
        OAUTH_CLIENT_SECRET_PROPERTY,
        OAUTH_ACCESS_TOKEN_PROPERTY,
        OAUTH_REFRESH_TOKEN_PROPERTY,
        OAUTH_TOKEN_ENDPOINT_URI_PROPERTY,
        ADDITIONAL_CONFIG_PROPERTY
    );

    /**
     * Common environment variables
     */
    String CLIENT_TYPE_ENV = "CLIENT_TYPE";
    String TRACING_TYPE_ENV = "TRACING_TYPE";

    String HTTP_JSON_CONTENT_TYPE = "application/vnd.kafka.json.v2+json";
}
