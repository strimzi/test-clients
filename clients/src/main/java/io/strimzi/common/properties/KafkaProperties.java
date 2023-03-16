/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.properties;

import io.strimzi.common.SaslType;
import io.strimzi.common.configuration.kafka.KafkaClientsConfiguration;
import io.strimzi.common.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.common.configuration.kafka.KafkaProducerConfiguration;
import io.strimzi.common.configuration.kafka.KafkaStreamsConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaProperties {
    public static Properties producerProperties(KafkaProducerConfiguration configuration) {
        Properties properties = clientProperties(configuration);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, configuration.getAcks());

        return properties;
    }

    public static Properties consumerProperties(KafkaConsumerConfiguration configuration) {
        Properties properties = clientProperties(configuration);

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, configuration.getClientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.getGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        if (configuration.getClientRack() != null) {
            properties.put(ConsumerConfig.CLIENT_RACK_CONFIG, configuration.getClientRack());
        }

        return properties;
    }

    public static Properties streamsProperties(KafkaStreamsConfiguration configuration) {
        Properties properties = clientProperties(configuration);

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, configuration.getApplicationId());
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, configuration.getCommitIntervalMs());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return properties;
    }

    public static Properties clientProperties(KafkaClientsConfiguration configuration) {
        Properties properties = new Properties();

        // Kubernetes Config Provider
        properties.put("config.providers", "secrets,configmaps");
        properties.put("config.providers.secrets.class", "io.strimzi.kafka.KubernetesSecretConfigProvider");
        properties.put("config.providers.configmaps.class", "io.strimzi.kafka.KubernetesConfigMapConfigProvider");

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, configuration.getBootstrapServers());
        properties = updatePropertiesWithSecurityConfiguration(properties, configuration);

        properties.putAll(configuration.getAdditionalConfig());

        return properties;
    }

    public static Properties updatePropertiesWithSecurityConfiguration(Properties properties, KafkaClientsConfiguration configuration) {
        properties = updatePropertiesWithTlsConfiguration(properties, configuration);

        if (shouldUpdatePropertiesWithSaslConfig(configuration)) {
            properties = updatePropertiesWithSaslConfiguration(properties, configuration);
        }
        if (shouldUpdatePropertiesWithOauthConfig(configuration)) {
            properties = updatePropertiesWithOauthConfig(properties, configuration);
        }

        return properties;
    }

    public static Properties updatePropertiesWithTlsConfiguration(Properties properties, KafkaClientsConfiguration configuration) {
        if (configuration.getSslTruststoreCertificate() != null) {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.toString());
            properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
            properties.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, configuration.getSslTruststoreCertificate());
        }

        if (configuration.getSslKeystoreCertificateChain() != null && configuration.getSslKeystoreKey() != null) {
            properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
            properties.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, configuration.getSslKeystoreCertificateChain());
            properties.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, configuration.getSslKeystoreKey());
        }

        return properties;
    }

    public static Properties updatePropertiesWithSaslConfiguration(Properties properties, KafkaClientsConfiguration configuration) {
        SaslType saslType = SaslType.getFromString(configuration.getSaslMechanism());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.toString());
        properties.put(SaslConfigs.SASL_MECHANISM, saslType.getKafkaProperty());

        String saslJaasConfig = configuration.getSaslJaasConfig();

        if (saslJaasConfig == null) {
            saslJaasConfig = saslType.equals(SaslType.PLAIN) ? PlainLoginModule.class.toString() : ScramLoginModule.class.toString();
            saslJaasConfig += String.format(" required username=%s password=%s", configuration.getSaslUserName(), configuration.getSaslPassword());

            if (saslType.equals(SaslType.SCRAM_SHA_512)) {
                saslJaasConfig += " algorithm=SHA-512";
            }

            saslJaasConfig += ";";
        }

        properties.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);

        return properties;
    }

    public static Properties updatePropertiesWithOauthConfig(Properties properties, KafkaClientsConfiguration configuration) {
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
        properties.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
        if (!(configuration.getAdditionalConfig().containsKey(SaslConfigs.SASL_MECHANISM) && configuration.getAdditionalConfig().getProperty(SaslConfigs.SASL_MECHANISM).equals("PLAIN"))) {
            properties.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, configuration.getSaslLoginCallbackClass());
        }

        return properties;
    }

    public static boolean shouldUpdatePropertiesWithSaslConfig(KafkaClientsConfiguration configuration) {
        return configuration.getSaslMechanism() != null
            && !configuration.getSaslMechanism().isEmpty()
            && SaslType.getAllSaslTypes().contains(configuration.getSaslMechanism());
    }

    @SuppressWarnings({"BooleanExpressionComplexity", "checkstyle:UnnecessaryParentheses"})
    public static boolean shouldUpdatePropertiesWithOauthConfig(KafkaClientsConfiguration configuration) {
        return (configuration.getOauthAccessToken() != null)
            || (configuration.getOauthTokenEndpointUri() != null && configuration.getOauthClientId() != null && configuration.getOauthRefreshToken() != null)
            || (configuration.getOauthTokenEndpointUri() != null && configuration.getOauthClientId() != null && configuration.getOauthClientSecret() != null);
    }

}
