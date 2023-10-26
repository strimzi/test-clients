/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.properties;

import io.strimzi.SaslType;
import io.strimzi.configuration.kafka.KafkaClientsConfiguration;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.configuration.ConfigurationConstants.ADDITIONAL_CONFIG_ENV;
import static io.strimzi.configuration.ConfigurationConstants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.configuration.ConfigurationConstants.CA_CRT_ENV;
import static io.strimzi.configuration.ConfigurationConstants.OAUTH_ACCESS_TOKEN_ENV;
import static io.strimzi.configuration.ConfigurationConstants.OAUTH_CLIENT_ID_ENV;
import static io.strimzi.configuration.ConfigurationConstants.OAUTH_CLIENT_SECRET_ENV;
import static io.strimzi.configuration.ConfigurationConstants.OAUTH_REFRESH_TOKEN_ENV;
import static io.strimzi.configuration.ConfigurationConstants.OAUTH_TOKEN_ENDPOINT_URI_ENV;
import static io.strimzi.configuration.ConfigurationConstants.SASL_JAAS_CONFIG_ENV;
import static io.strimzi.configuration.ConfigurationConstants.SASL_MECHANISM_ENV;
import static io.strimzi.configuration.ConfigurationConstants.USER_CRT_ENV;
import static io.strimzi.configuration.ConfigurationConstants.USER_KEY_ENV;
import static io.strimzi.configuration.ConfigurationConstants.USER_NAME_ENV;
import static io.strimzi.configuration.ConfigurationConstants.USER_PASSWORD_ENV;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicKafkaPropertiesTest {

    @Test
    void testShouldConfigureOauthCheck() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty properties will not configure Oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(OAUTH_ACCESS_TOKEN_ENV, "access-token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties with just Oauth access token will configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));

        configuration.remove(OAUTH_ACCESS_TOKEN_ENV);
        configuration.put(OAUTH_TOKEN_ENDPOINT_URI_ENV, "localhost:9090/path/to/token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri will not configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(OAUTH_CLIENT_ID_ENV, "client-id");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri + client id will not configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(OAUTH_REFRESH_TOKEN_ENV, "refresh token");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and with Oauth token endpoint uri + client id + refresh token will configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));

        configuration.remove(OAUTH_REFRESH_TOKEN_ENV);

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and just with Oauth token endpoint uri + client id will not configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(false));

        configuration.put(OAUTH_CLIENT_SECRET_ENV, "client secret");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that properties without Oauth access token and with Oauth token endpoint uri + client id + client secret will configure oauth
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithOauthConfig(kafkaClientsConfiguration), is(true));
    }

    @Test
    void testShouldConfigureSaslCheck() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty properties will not configure SASL
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(SASL_MECHANISM_ENV, "");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that empty SASL mechanism env will not configure SASL
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(SASL_MECHANISM_ENV, "random");

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that wrong value in SASL mechanism env will not configure SASL
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(false));

        configuration.put(SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_256.getName());

        kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check that with correct SASL mechanism in the env it will configure SASL
        assertThat(BasicKafkaProperties.shouldUpdatePropertiesWithSaslConfig(kafkaClientsConfiguration), is(true));
    }

    @Test
    void testConfigureCommonProperties() {
        String bootstrapServer = "my-cluster-kafka:9092";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, bootstrapServer);

        KafkaClientsConfiguration kafkaClientsConfiguration = new KafkaClientsConfiguration(configuration);

        // check properties properties
        Properties commonProperties = BasicKafkaProperties.clientProperties(kafkaClientsConfiguration);

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
        properties = BasicKafkaProperties.updatePropertiesWithTlsConfiguration(properties, clientsConfiguration);

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
        properties = BasicKafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        assertThat(properties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is(SecurityProtocol.SASL_SSL.toString()));
        assertThat(properties.getProperty(SaslConfigs.SASL_MECHANISM), is(SaslType.SCRAM_SHA_512.getKafkaProperty()));
        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(saslJaasConfig));

        configuration.remove(SASL_JAAS_CONFIG_ENV);
        configuration.put(USER_NAME_ENV, userName);
        configuration.put(USER_PASSWORD_ENV, userPassword);

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = BasicKafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(expectedJaas));

        configuration.put(SASL_MECHANISM_ENV, SaslType.SCRAM_SHA_256.getName());

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = BasicKafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

        expectedJaas = expectedJaas.replace(" algorithm=SHA-512", "");

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is(expectedJaas));

        configuration.put(SASL_MECHANISM_ENV, SaslType.PLAIN.getName());

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = BasicKafkaProperties.updatePropertiesWithSaslConfiguration(properties, clientsConfiguration);

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
        properties = BasicKafkaProperties.updatePropertiesWithOauthConfig(properties, clientsConfiguration);

        assertThat(properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG), is("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;"));
        assertThat(properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is("SASL_PLAINTEXT"));
        assertThat(properties.getProperty(SaslConfigs.SASL_MECHANISM), is("OAUTHBEARER"));
        assertThat(properties.getProperty(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS), is("io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler"));

        configuration.put(ADDITIONAL_CONFIG_ENV, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG + "= SSL\n" + SaslConfigs.SASL_MECHANISM + "= PLAIN");

        clientsConfiguration = new KafkaClientsConfiguration(configuration);
        properties = new Properties();
        properties = BasicKafkaProperties.updatePropertiesWithOauthConfig(properties, clientsConfiguration);

        assertThat(properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG), is("SASL_PLAINTEXT"));
        assertThat(properties.getProperty(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS), nullValue());
    }
}
