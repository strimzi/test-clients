/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration.kafka;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseIntOrDefault;
import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseLongOrDefault;
import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseMapOfProperties;
import static io.strimzi.testclients.configuration.ClientsConfigurationUtils.parseStringOrDefault;
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
import static io.strimzi.testclients.configuration.ConfigurationConstants.TRACING_TYPE_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_CRT_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_KEY_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_NAME_ENV;
import static io.strimzi.testclients.configuration.ConfigurationConstants.USER_PASSWORD_ENV;

public class KafkaClientsConfiguration {
    private final String bootstrapServers;
    private final long delayMs;
    private final int messageCount;
    private final String sslTruststoreCertificate;
    private final String sslKeystoreKey;
    private final String sslKeystoreCertificateChain;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;
    private final Properties additionalConfig;
    private final String saslMechanism;
    private final String saslJaasConfig;
    private final String saslUserName;
    private final String saslPassword;
    private final boolean tracingEnabled;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";

    public KafkaClientsConfiguration(Map<String, String> map) {
        this.bootstrapServers = map.get(BOOTSTRAP_SERVERS_ENV);
        this.delayMs = parseLongOrDefault(map.get(DELAY_MS_ENV), DEFAULT_DELAY_MS);
        this.messageCount = parseIntOrDefault(map.get(MESSAGE_COUNT_ENV), DEFAULT_MESSAGES_COUNT);
        this.sslTruststoreCertificate = map.get(CA_CRT_ENV);
        this.sslKeystoreKey = map.get(USER_KEY_ENV);
        this.sslKeystoreCertificateChain = map.get(USER_CRT_ENV);
        this.oauthClientId = map.get(OAUTH_CLIENT_ID_ENV);
        this.oauthClientSecret = map.get(OAUTH_CLIENT_SECRET_ENV);
        this.oauthAccessToken = map.get(OAUTH_ACCESS_TOKEN_ENV);
        this.oauthRefreshToken = map.get(OAUTH_REFRESH_TOKEN_ENV);
        this.oauthTokenEndpointUri = map.get(OAUTH_TOKEN_ENDPOINT_URI_ENV);
        this.saslMechanism = map.get(SASL_MECHANISM_ENV);
        this.saslJaasConfig = map.get(SASL_JAAS_CONFIG_ENV);
        this.saslUserName = map.get(USER_NAME_ENV);
        this.saslPassword = map.get(USER_PASSWORD_ENV);
        this.additionalConfig = parseMapOfProperties(parseStringOrDefault(map.get(ADDITIONAL_CONFIG_ENV), ""));
        this.tracingEnabled = map.get(TRACING_TYPE_ENV) != null;

        if (bootstrapServers == null || bootstrapServers.isEmpty()) throw new InvalidParameterException("Bootstrap servers are not set");
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getSslTruststoreCertificate() {
        return sslTruststoreCertificate;
    }

    public String getSslKeystoreKey() {
        return sslKeystoreKey;
    }

    public String getSslKeystoreCertificateChain() {
        return sslKeystoreCertificateChain;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public String getOauthAccessToken() {
        return oauthAccessToken;
    }

    public String getOauthRefreshToken() {
        return oauthRefreshToken;
    }

    public String getOauthTokenEndpointUri() {
        return oauthTokenEndpointUri;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }

    public String getSaslUserName() {
        return saslUserName;
    }

    public String getSaslPassword() {
        return saslPassword;
    }

    public Properties getAdditionalConfig() {
        return additionalConfig;
    }

    public String getSaslLoginCallbackClass() {
        return saslLoginCallbackClass;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    @Override
    public String toString() {
        String sslTruststoreCertificate =
            this.getSslTruststoreCertificate() == null ? null : "[CA CRT]";

        String sslKeystoreKey =
            this.getSslKeystoreKey() == null ? null : "[USER KEY]";

        String sslKeystoreCertificateChain =
            this.getSslKeystoreCertificateChain() == null ? null : "[USER CERT]";

        String oauthClientSecret =
            this.getOauthClientSecret() == null ? null : "[OAUTH CLIENT SECRET]";

        String oauthAccessToken =
            this.getOauthAccessToken() == null ? null : "[OAUTH ACCESS TOKEN]";

        String oauthRefreshToken =
            this.getOauthRefreshToken() == null ? null : "[OAUTH REFRESH TOKEN]";

        String saslJaasConfig =
            this.getSaslJaasConfig() == null ? null : "[SASL JAAS CONFIG]";

        String saslPassword =
            this.getSaslPassword() == null ? null : "[SASL PASSWORD]";

        return "bootstrapServers='" + this.getBootstrapServers() + "',\n" +
            "delayMs='" + this.getDelayMs() + "',\n" +
            "messageCount='" + this.getMessageCount() + "',\n" +
            "sslTruststoreCertificate='" + sslTruststoreCertificate + "',\n" +
            "sslKeystoreKey='" + sslKeystoreKey + "',\n" +
            "sslKeystoreCertificateChain='" + sslKeystoreCertificateChain + "',\n" +
            "oauthClientId='" + this.getOauthClientId() + "',\n" +
            "oauthClientSecret='" + oauthClientSecret + "',\n" +
            "oauthAccessToken='" + oauthAccessToken + "',\n" +
            "oauthRefreshToken='" + oauthRefreshToken + "',\n" +
            "oauthTokenEndpointUri='" + this.getOauthTokenEndpointUri() + "',\n" +
            "saslMechanism='" + this.getSaslMechanism() + "',\n" +
            "saslJaasConfig='" + saslJaasConfig + "',\n" +
            "saslUserName='" + this.getSaslUserName() + "',\n" +
            "saslPassword='" + saslPassword + "',\n" +
            "tracingEnabled='" + this.isTracingEnabled() + "',\n" +
            "additionalConfig='" + this.getAdditionalConfig() + "'";
    }
}
