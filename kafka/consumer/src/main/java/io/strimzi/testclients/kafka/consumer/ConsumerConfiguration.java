/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.kafka.consumer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

public class ConsumerConfiguration {
    private static final Logger LOGGER = LogManager.getLogger(ConsumerConfiguration.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final String autoOffsetReset = "earliest";
    private final String enableAutoCommit = "false";
    private final String clientRack;
    private final Long messageCount;
    private final String sslTruststoreCertificates;
    private final String sslKeystoreKey;
    private final String sslKeystoreCertificateChain;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;
    private final String additionalConfig;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";


    public ConsumerConfiguration() {
        this.bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        this.topic = System.getenv("TOPIC");
        this.groupId = System.getenv("GROUP_ID");
        this.clientRack = System.getenv("CLIENT_RACK");
        this.messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        this.sslTruststoreCertificates = System.getenv("CA_CRT");
        this.sslKeystoreKey = System.getenv("USER_KEY");
        this.sslKeystoreCertificateChain = System.getenv("USER_CRT");
        this.oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        this.oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        this.oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        this.oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        this.oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");
        this.additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");
    }

    @SuppressWarnings({"BooleanExpressionComplexity", "checkstyle:UnnecessaryParentheses"})
    public static Properties createProperties(ConsumerConfiguration config) {
        Properties props = new Properties();

        // Kubernetes Config Provider
        props.put("config.providers", "secrets,configmaps");
        props.put("config.providers.secrets.class", "io.strimzi.kafka.KubernetesSecretConfigProvider");
        props.put("config.providers.configmaps.class", "io.strimzi.kafka.KubernetesConfigMapConfigProvider");

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        if (config.getClientRack() != null) {
            props.put(ConsumerConfig.CLIENT_RACK_CONFIG, config.getClientRack());
        }
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getEnableAutoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        if (config.getSslTruststoreCertificates() != null)   {
            LOGGER.info("Configuring truststore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
            props.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, config.getSslTruststoreCertificates());
        }

        if (config.getSslKeystoreCertificateChain() != null && config.getSslKeystoreKey() != null)   {
            LOGGER.info("Configuring keystore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
            props.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, config.getSslKeystoreCertificateChain());
            props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, config.getSslKeystoreKey());
        }

        Properties additionalProps = new Properties();
        if (!config.getAdditionalConfig().isEmpty()) {
            StringTokenizer tok = new StringTokenizer(config.getAdditionalConfig(), System.lineSeparator());
            while (tok.hasMoreTokens()) {
                String record = tok.nextToken();
                int endIndex = record.indexOf('=');
                if (endIndex == -1) {
                    throw new RuntimeException("Failed to parse Map from String");
                }
                String key = record.substring(0, endIndex);
                String value = record.substring(endIndex + 1);
                additionalProps.put(key.trim(), value.trim());
            }
        }

        if ((config.getOauthAccessToken() != null)
            || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthRefreshToken() != null)
            || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthClientSecret() != null))    {
            LOGGER.info("Configuring OAuth");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            if (!(additionalProps.containsKey(SaslConfigs.SASL_MECHANISM) && additionalProps.getProperty(SaslConfigs.SASL_MECHANISM).equals("PLAIN"))) {
                props.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, config.saslLoginCallbackClass);
            }
        }

        // override properties with defined additional properties
        props.putAll(additionalProps);

        return props;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public String getClientRack() {
        return clientRack;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public String getSslTruststoreCertificates() {
        return sslTruststoreCertificates;
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

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    @Override
    public String toString() {
        return "ConsumerConfiguration{" +
            "bootstrapServers='" + getBootstrapServers() + '\'' +
            ", topic='" + getTopic() + '\'' +
            ", groupId='" + getGroupId() + '\'' +
            ", autoOffsetReset='" + getAutoOffsetReset() + '\'' +
            ", enableAutoCommit='" + getEnableAutoCommit() + '\'' +
            ", clientRack='" + getClientRack() + '\'' +
            ", messageCount=" + getMessageCount() +
            ", sslTruststoreCertificates='" + getSslTruststoreCertificates() + '\'' +
            ", sslKeystoreKey='" + getSslKeystoreKey() + '\'' +
            ", sslKeystoreCertificateChain='" + getSslKeystoreCertificateChain() + '\'' +
            ", oauthClientId='" + getOauthClientId() + '\'' +
            ", oauthClientSecret='" + getOauthClientSecret() + '\'' +
            ", oauthAccessToken='" + getOauthAccessToken() + '\'' +
            ", oauthRefreshToken='" + getOauthRefreshToken() + '\'' +
            ", oauthTokenEndpointUri='" + getOauthTokenEndpointUri() + '\'' +
            ", additionalConfig='" + getAdditionalConfig() + '\'' +
            '}';
    }
}
