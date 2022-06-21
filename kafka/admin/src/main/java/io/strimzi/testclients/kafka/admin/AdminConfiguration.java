/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.kafka.admin;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

public class AdminConfiguration {
    private static final Logger log = LogManager.getLogger(AdminConfiguration.class);

    private final String bootstrapServers;
    private final String topicOperation;
    private final String topic;
    private final String topicPartitions;
    private final String replicationFactor;
    private final String numberOfTopics;
    private final String topicOffset;
    private final String compact;
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

    public AdminConfiguration() {
        this.bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        this.topic = System.getenv("TOPIC");
        this.topicPartitions = System.getenv().getOrDefault("PARTITIONS", "1");
        this.replicationFactor = System.getenv().getOrDefault("REPLICATION_FACTOR", "1");
        this.numberOfTopics = System.getenv().getOrDefault("TOPICS_COUNT", "1");
        this.topicOffset = System.getenv().getOrDefault("TOPIC_OFFSET", "0");
        this.compact = System.getenv().getOrDefault("TOPIC_COMPACT", "true");
        this.topicOperation = System.getenv("TOPIC_OPERATION");
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
    public static Properties createProperties(AdminConfiguration config) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());

        // Kubernetes Config Provider
        props.put("config.providers", "secrets,configmaps");
        props.put("config.providers.secrets.class", "io.strimzi.kafka.KubernetesSecretConfigProvider");
        props.put("config.providers.configmaps.class", "io.strimzi.kafka.KubernetesConfigMapConfigProvider");

        if (config.getSslTruststoreCertificates() != null)   {
            log.info("Configuring truststore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
            props.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, config.getSslTruststoreCertificates());
        }

        if (config.getSslKeystoreCertificateChain() != null && config.getSslKeystoreKey() != null)   {
            log.info("Configuring keystore");
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
            log.info("Configuring OAuth");
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

    public String getTopicOperation() {
        return topicOperation;
    }

    public String getTopic() {
        return topic;
    }

    public String getTopicPartitions() {
        return topicPartitions;
    }

    public String getReplicationFactor() {
        return replicationFactor;
    }

    public String getNumberOfTopics() {
        return numberOfTopics;
    }

    public java.lang.String getTopicOffset() {
        return topicOffset;
    }

    public String getCompact() {
        return compact;
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
        return "KafkaAdminConfig{" +
            "bootstrapServers='" + getBootstrapServers() + '\'' +
            ", topicOperation='" + getTopicOperation() + '\'' +
            ", numberOfTopics='" + getNumberOfTopics() + '\'' +
            ", topicOffset='" + getTopicOffset() + '\'' +
            ", topic='" + getTopic() + '\'' +
            ", topicPartitions='" + getTopicPartitions() + '\'' +
            ", replicationFactor='" + getReplicationFactor() + '\'' +
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
