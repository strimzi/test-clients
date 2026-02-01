/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration.kafka;

import io.strimzi.testclients.SaslType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.testclients.configuration.ConfigurationConstants.ADDITIONAL_CONFIG_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.CA_CRT_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.DEFAULT_DELAY_MS;
import static io.strimzi.testclients.configuration.ConfigurationConstants.DEFAULT_MESSAGES_COUNT;
import static io.strimzi.testclients.configuration.ConfigurationConstants.DELAY_MS_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.MESSAGE_COUNT_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.OAUTH_ACCESS_TOKEN_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.OAUTH_CLIENT_ID_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.OAUTH_CLIENT_SECRET_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.OAUTH_REFRESH_TOKEN_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.OAUTH_TOKEN_ENDPOINT_URI_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.SASL_JAAS_CONFIG_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.SASL_MECHANISM_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_CRT_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_KEY_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_NAME_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_PASSWORD_ENV;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KafkaClientsConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        assertThat(kafkaClientsConfiguration.getDelayMs(), is(DEFAULT_DELAY_MS));
        assertThat(kafkaClientsConfiguration.getMessageCount(), is(DEFAULT_MESSAGES_COUNT));
        assertThat(kafkaClientsConfiguration.getAdditionalConfig(), is(new Properties()));
    }

    @Test
    void testCustomConfiguration() {
        String bootstrapServer = "my-cluster-kafka:9092";
        String accessToken = "access-token";
        String oauthTokenEndpointUri = "localhost:9090/path/to/token";
        String oauthClientId = "client-id";
        String refreshToken = "refresh token";
        String clientSecret = "client secret";
        String userName = "arnost";
        String userPassword = "completely-top-secret";
        String saslJaasConfig = "my-sasl-config";
        String sslTruststoreCert = "my-cert";
        String sslKeystoreCert = "my-user-cert";
        String sslKeystoreKey = "my-user-key";
        String additionalProperties = "my-key=my-value";
        int messageCount = 678;
        long delayMs = 68899;

        Properties expectedAdditionalProps = new Properties();
        expectedAdditionalProps.put("my-key", "my-value");

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_512.getName());
        configuration.put(USER_NAME_ENV, userName);
        configuration.put(USER_PASSWORD_ENV, userPassword);
        configuration.put(SASL_JAAS_CONFIG_ENV, saslJaasConfig);
        configuration.put(OAUTH_ACCESS_TOKEN_ENV, accessToken);
        configuration.put(OAUTH_TOKEN_ENDPOINT_URI_ENV, oauthTokenEndpointUri);
        configuration.put(OAUTH_CLIENT_ID_ENV, oauthClientId);
        configuration.put(OAUTH_REFRESH_TOKEN_ENV, refreshToken);
        configuration.put(OAUTH_CLIENT_SECRET_ENV, clientSecret);
        configuration.put(CA_CRT_ENV, sslTruststoreCert);
        configuration.put(USER_CRT_ENV, sslKeystoreCert);
        configuration.put(USER_KEY_ENV, sslKeystoreKey);
        configuration.put(ADDITIONAL_CONFIG_ENV, additionalProperties);
        configuration.put(MESSAGE_COUNT_ENV, String.valueOf(messageCount));
        configuration.put(DELAY_MS_ENV, String.valueOf(delayMs));

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        assertThat(kafkaClientsConfiguration.getBootstrapServers(), is(bootstrapServer));
        assertThat(kafkaClientsConfiguration.getSaslMechanism(), is(SaslType.SCRAM_SHA_512.getName()));
        assertThat(kafkaClientsConfiguration.getSaslUserName(), is(userName));
        assertThat(kafkaClientsConfiguration.getSaslPassword(), is(userPassword));
        assertThat(kafkaClientsConfiguration.getSaslJaasConfig(), is(saslJaasConfig));
        assertThat(kafkaClientsConfiguration.getOauthAccessToken(), is(accessToken));
        assertThat(kafkaClientsConfiguration.getOauthTokenEndpointUri(), is(oauthTokenEndpointUri));
        assertThat(kafkaClientsConfiguration.getOauthClientId(), is(oauthClientId));
        assertThat(kafkaClientsConfiguration.getOauthRefreshToken(), is(refreshToken));
        assertThat(kafkaClientsConfiguration.getOauthClientSecret(), is(clientSecret));
        assertThat(kafkaClientsConfiguration.getSslTruststoreCertificate(), is(sslTruststoreCert));
        assertThat(kafkaClientsConfiguration.getSslKeystoreCertificateChain(), is(sslKeystoreCert));
        assertThat(kafkaClientsConfiguration.getSslKeystoreKey(), is(sslKeystoreKey));
        assertThat(kafkaClientsConfiguration.getAdditionalConfig(), is(expectedAdditionalProps));
        assertThat(kafkaClientsConfiguration.getMessageCount(), is(messageCount));
        assertThat(kafkaClientsConfiguration.getDelayMs(), is(delayMs));
    }

    @Test
    void testInvalidConfiguration() {
        String bootstrapServer = "my-cluster-kafka:9092";
        String delayMs = "this will not work";
        String messageCount = "this too";
        int additionalProps = 25;

        Map<String, String> configuration = new HashMap<>();

        assertThrows(RuntimeException.class, () -> new KafkaClientsConfiguration(configuration));

        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(DELAY_MS_ENV, delayMs);
        configuration.put(MESSAGE_COUNT_ENV, messageCount);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        assertThat(kafkaClientsConfiguration.getDelayMs(), is(DEFAULT_DELAY_MS));
        assertThat(kafkaClientsConfiguration.getMessageCount(), is(DEFAULT_MESSAGES_COUNT));

        configuration.put(ADDITIONAL_CONFIG_ENV, String.valueOf(additionalProps));

        assertThrows(RuntimeException.class, () -> new KafkaClientsConfiguration(configuration));
    }
}
