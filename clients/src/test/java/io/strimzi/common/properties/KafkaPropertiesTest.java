/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.properties;

import io.strimzi.common.SaslType;
import io.strimzi.common.configuration.Constants;
import io.strimzi.common.configuration.kafka.KafkaClientsConfiguration;
import io.strimzi.common.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.common.configuration.kafka.KafkaProducerConfiguration;

import static io.strimzi.common.configuration.Constants.ADDITIONAL_CONFIG_ENV;
import static io.strimzi.common.configuration.Constants.APPLICATION_ID_ENV;
import static io.strimzi.common.configuration.Constants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.common.configuration.Constants.CA_CRT_ENV;
import static io.strimzi.common.configuration.Constants.CLIENT_ID_ENV;
import static io.strimzi.common.configuration.Constants.CLIENT_RACK_ENV;
import static io.strimzi.common.configuration.Constants.COMMIT_INTERVAL_MS_ENV;
import static io.strimzi.common.configuration.Constants.DEFAULT_CLIENT_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_COMMIT_INTERVAL_MS;
import static io.strimzi.common.configuration.Constants.DEFAULT_GROUP_ID;
import static io.strimzi.common.configuration.Constants.DEFAULT_PRODUCER_ACKS;
import static io.strimzi.common.configuration.Constants.GROUP_ID_ENV;
import static io.strimzi.common.configuration.Constants.OAUTH_ACCESS_TOKEN_ENV;
import static io.strimzi.common.configuration.Constants.OAUTH_CLIENT_ID_ENV;
import static io.strimzi.common.configuration.Constants.OAUTH_CLIENT_SECRET_ENV;
import static io.strimzi.common.configuration.Constants.OAUTH_REFRESH_TOKEN_ENV;
import static io.strimzi.common.configuration.Constants.OAUTH_TOKEN_ENDPOINT_URI_ENV;
import static io.strimzi.common.configuration.Constants.PRODUCER_ACKS_ENV;
import static io.strimzi.common.configuration.Constants.SASL_JAAS_CONFIG_ENV;
import static io.strimzi.common.configuration.Constants.SASL_MECHANISM_ENV;
import static io.strimzi.common.configuration.Constants.SOURCE_TOPIC_ENV;
import static io.strimzi.common.configuration.Constants.TARGET_TOPIC_ENV;
import static io.strimzi.common.configuration.Constants.TOPIC_ENV;
import static io.strimzi.common.configuration.Constants.USER_CRT_ENV;
import static io.strimzi.common.configuration.Constants.USER_KEY_ENV;
import static io.strimzi.common.configuration.Constants.USER_NAME_ENV;
import static io.strimzi.common.configuration.Constants.USER_PASSWORD_ENV;
import static org.hamcrest.CoreMatchers.is;

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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class KafkaPropertiesTest {

    @Test
    void testShouldConfigureOauthCheck() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty properties will not configure Oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.OAUTH_ACCESS_TOKEN_ENV, "access-token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties with just Oauth access token will configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));

        configuration.remove(Constants.OAUTH_ACCESS_TOKEN_ENV);
        configuration.put(Constants.OAUTH_TOKEN_ENDPOINT_URI_ENV, "localhost:9090/path/to/token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri will not configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.OAUTH_CLIENT_ID_ENV, "client-id");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri + client id will not configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.OAUTH_REFRESH_TOKEN_ENV, "refresh token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and with Oauth token endpoint uri + client id + refresh token will configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));

        configuration.remove(Constants.OAUTH_REFRESH_TOKEN_ENV);

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri + client id will not configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.OAUTH_CLIENT_SECRET_ENV, "client secret");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and with Oauth token endpoint uri + client id + client secret will configure oauth
        assertThat(KafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));
    }

    @Test
    void testShouldConfigureSaslCheck() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty properties will not configure SASL
        assertThat(KafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.SASL_MECHANISM_ENV, "");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty SASL mechanism env will not configure SASL
        assertThat(KafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.SASL_MECHANISM_ENV, "random");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that wrong value in SASL mechanism env will not configure SASL
        assertThat(KafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(Constants.SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_256.getName());

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that with correct SASL mechanism in the env it will configure SASL
        assertThat(KafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(true));
    }

    @Test
    void testConfigureProducerProperties() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");

        KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        // check default properties
        Properties producerProperties = KafkaProperties.producerProperties(kafkaProducerConfiguration);

        assertThat(producerProperties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), is(StringSerializer.class.getName()));
        assertThat(producerProperties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG), is(StringSerializer.class.getName()));
        assertThat(producerProperties.getProperty(ProducerConfig.ACKS_CONFIG), is(DEFAULT_PRODUCER_ACKS));

        String producerAcks = "0";
        configuration.put(PRODUCER_ACKS_ENV, producerAcks);

        kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
        producerProperties = KafkaProperties.producerProperties(kafkaProducerConfiguration);

        // check custom properties
        assertThat(producerProperties.getProperty(ProducerConfig.ACKS_CONFIG), is(producerAcks));
    }

    @Test
    void testConfigureConsumerProperties() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        // check default properties
        Properties consumerProperties = KafkaProperties.consumerProperties(kafkaConsumerConfiguration);

        assertThat(consumerProperties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG), is(StringDeserializer.class.getName()));
        assertThat(consumerProperties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG), is(StringDeserializer.class.getName()));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG), is(DEFAULT_CLIENT_ID));
        assertThat(consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), is(DEFAULT_GROUP_ID));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_RACK_CONFIG), nullValue());
        assertThat(consumerProperties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), is("earliest"));
        assertThat(consumerProperties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), is("false"));

        String clientId = "random-client-id";
        String groupId = "random-group-id";
        String clientRack = "rack-0";

        configuration.put(CLIENT_ID_ENV, clientId);
        configuration.put(GROUP_ID_ENV, groupId);
        configuration.put(CLIENT_RACK_ENV, clientRack);

        kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        consumerProperties = KafkaProperties.consumerProperties(kafkaConsumerConfiguration);

        // check custom properties
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG), is(clientId));
        assertThat(consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), is(groupId));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_RACK_CONFIG), is(clientRack));
    }

    @Test
    void testConfigureStreamsProperties() {
        String appId = "my-app-0";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(SOURCE_TOPIC_ENV, "source-my-topic");
        configuration.put(TARGET_TOPIC_ENV, "target-my-topic");
        configuration.put(APPLICATION_ID_ENV, appId);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        // check default properties
        Properties streamsProperties = KafkaProperties.streamsProperties(kafkaStreamsConfiguration);

        assertThat(streamsProperties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG), is(appId));
        assertThat(streamsProperties.get(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG), is(DEFAULT_COMMIT_INTERVAL_MS));
        assertThat(streamsProperties.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG).toString(), is(Serdes.String().getClass().toString()));
        assertThat(streamsProperties.get(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG).toString(), is(Serdes.String().getClass().toString()));

        long commitInterval = 7000L;
        configuration.put(COMMIT_INTERVAL_MS_ENV, String.valueOf(commitInterval));

        kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        streamsProperties = KafkaProperties.streamsProperties(kafkaStreamsConfiguration);

        // check custom properties
        assertThat(streamsProperties.get(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG), is(commitInterval));
    }

    @Test
    void testConfigureCommonProperties() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check properties properties
        Properties commonProperties = KafkaProperties.clientProperties(kafkaClientsConfiguration);

        assertThat(commonProperties.get("config.providers"), is("secrets,configmaps"));
        assertThat(commonProperties.get("config.providers.secrets.class"), is("io.strimzi.kafka.KubernetesSecretConfigProvider"));
        assertThat(commonProperties.get("config.providers.configmaps.class"), is("io.strimzi.kafka.KubernetesConfigMapConfigProvider"));
        assertThat(commonProperties.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG), is(bootstrapServer));
    }

    @Test
    void testUpdatePropertiesWithTlsConfiguration() {
        String bootstrapServer = "my-cluster-kafka:9092";
        String sslTruststoreCert = "my-cert";
        String sslKeystoreCert = "my-user-cert";
        String sslKeystoreKey = "my-user-key";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(CA_CRT_ENV, sslTruststoreCert);
        configuration.put(USER_CRT_ENV, sslKeystoreCert);
        configuration.put(USER_KEY_ENV, sslKeystoreKey);

        KafkaClientsConfiguration clientsConfiguration = new KafkaClientsConfiguration(configuration);
        Properties properties = new Properties();
        properties = KafkaProperties.updatePropertiesWithTlsConfiguration(properties, clientsConfiguration);

        assertThat(properties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is(SecurityProtocol.SSL.toString()));
        assertThat(properties.getProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG), is("PEM"));
        assertThat(properties.getProperty(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG), is(sslTruststoreCert));

        assertThat(properties.getProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG), is("PEM"));
        assertThat(properties.getProperty(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG), is(sslKeystoreCert));
        assertThat(properties.getProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG), is(sslKeystoreKey));
    }

    @Test
    void testUpdatePropertiesWithSaslConfiguration() {
        String bootstrapServer = "my-cluster-kafka:9092";
        String saslJaasConfig = "my-sasl-config";
        String userName = "arnost";
        String userPassword = "completely-top-secret";
        String expectedJaas = ScramLoginModule.class + String.format(" required username=%s password=%s algorithm=SHA-512;", userName, userPassword);

        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        configuration.put(SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_512.getName());
        configuration.put(SASL_JAAS_CONFIG_ENV, saslJaasConfig);

        KafkaClientsConfiguration clientsConfiguration = new KafkaClientsConfiguration(configuration);
        Properties properties = new Properties();
        properties = KafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        assertThat(properties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is(SecurityProtocol.SASL_SSL.toString()));
        assertThat(properties.getProperty(SaslConfigs.SASL_MECHANISM), is(SaslType.SCRAM_SHA_512.getKafkaProperty()));
        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(saslJaasConfig));

        configuration.remove(SASL_JAAS_CONFIG_ENV);
        configuration.put(USER_NAME_ENV, userName);
        configuration.put(USER_PASSWORD_ENV, userPassword);

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = KafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(expectedJaas));

        configuration.put(SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_256.getName());

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = KafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        expectedJaas = expectedJaas.replace(" algorithm=SHA-512", "");

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(expectedJaas));

        configuration.put(SASL_MECHANISM_ENV, SaslType.PLAIN.getName());

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = KafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        expectedJaas = expectedJaas.replace(ScramLoginModule.class.toString(), PlainLoginModule.class.toString());

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(expectedJaas));
    }

    @Test
    void testUpdatePropertiesWithOauthConfig() {
        String bootstrapServer = "my-cluster-kafka:9092";
        String accessToken = "access-token";
        String oauthTokenEndpointUri = "localhost:9090/path/to/token";
        String oauthClientId = "client-id";
        String refreshToken = "refresh token";
        String clientSecret = "client secret";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(OAUTH_ACCESS_TOKEN_ENV, accessToken);
        configuration.put(OAUTH_TOKEN_ENDPOINT_URI_ENV, oauthTokenEndpointUri);
        configuration.put(OAUTH_CLIENT_ID_ENV, oauthClientId);
        configuration.put(OAUTH_REFRESH_TOKEN_ENV, refreshToken);
        configuration.put(OAUTH_CLIENT_SECRET_ENV, clientSecret);
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration clientsConfiguration = new KafkaClientsConfiguration(configuration);
        Properties properties = new Properties();
        properties = KafkaProperties.updatePropertiesWithOauthConfig(properties, clientsConfiguration);

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;"));
        assertThat(properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is("SASL_PLAINTEXT"));
        assertThat(properties.getProperty(SaslConfigs.SASL_MECHANISM), is("OAUTHBEARER"));
        assertThat(properties.getProperty(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS), is("io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler"));

        configuration.put(ADDITIONAL_CONFIG_ENV, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG + "= SSL\n" + SaslConfigs.SASL_MECHANISM + "= PLAIN");

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = new Properties();
        properties = KafkaProperties.updatePropertiesWithOauthConfig(properties, clientsConfiguration);

        assertThat(properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is("SASL_PLAINTEXT"));
        assertThat(properties.getProperty(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS), nullValue());
    }
}
