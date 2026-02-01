/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.kafka;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.Image;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KafkaAdminClientTest {

    @Test
    void testClientBuilder() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        List<EnvVar> additionalEnvVars = List.of(
            new EnvVarBuilder()
                .withName("RANDOM")
                .withValue("value")
                .build(),
            new EnvVarBuilder()
                .withName("SOME")
                .withValue("thing")
                .build()
        );
        String additionalConfig = "my.config = value";
        String configFolderPath = "/tmp";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withAdditionalEnvVars(additionalEnvVars)
            .withAdditionalConfig(additionalConfig)
            .withConfigFolderPath(configFolderPath)
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(deployment.getMetadata().getName(), is(name));
        assertThat(deployment.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(5));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.ADDITIONAL_CONFIG_ENV), is(additionalConfig));
        assertThat(envVars.get(ConfigurationConstants.CONFIG_FOLDER_PATH_ENV), is(configFolderPath));

        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));
    }

    @Test
    void testConfiguringImage() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(deployment.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));
    }

    @Test
    void testConfiguringOAuth() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";

        String clientId = "client-id";
        String accessToken = "my-access-token";
        String clientSecret = "client-secret";
        String refreshToken = "my-refresh-token";
        String endpointUri = "localhost:8080";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withNewOauth()
                .withOauthClientId(clientId)
                .withOauthAccessToken(accessToken)
                .withOauthClientSecret(clientSecret)
                .withOauthRefreshToken(refreshToken)
                .withOauthTokenEndpointUri(endpointUri)
            .endOauth()
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(6));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));

        assertThat(envVars.get(ConfigurationConstants.OAUTH_CLIENT_ID_ENV), is(clientId));
        assertThat(envVars.get(ConfigurationConstants.OAUTH_ACCESS_TOKEN_ENV), is(accessToken));
        assertThat(envVars.get(ConfigurationConstants.OAUTH_CLIENT_SECRET_ENV), is(clientSecret));
        assertThat(envVars.get(ConfigurationConstants.OAUTH_REFRESH_TOKEN_ENV), is(refreshToken));
        assertThat(envVars.get(ConfigurationConstants.OAUTH_TOKEN_ENDPOINT_URI_ENV), is(endpointUri));
    }

    @Test
    void testConfiguringSasl() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";

        String jaasConfig = "jaas-config";
        String mechanism = "plain";
        String password = "tajne";
        String username = "arnost";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withNewSasl()
                .withSaslJaasConfig(jaasConfig)
                .withSaslMechanism(mechanism)
                .withSaslPassword(password)
                .withSaslUserName(username)
            .endSasl()
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(5));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));

        assertThat(envVars.get(ConfigurationConstants.SASL_JAAS_CONFIG_ENV), is(jaasConfig));
        assertThat(envVars.get(ConfigurationConstants.SASL_MECHANISM_ENV), is(mechanism));
        assertThat(envVars.get(ConfigurationConstants.USER_NAME_ENV), is(username));
        assertThat(envVars.get(ConfigurationConstants.USER_PASSWORD_ENV), is(password));
    }

    @Test
    void testConfiguringSsl() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";

        String truststore = "truststore-certificate";
        String keystoreCert = "keystore-certificate";
        String keystoreKey = "keystore-key";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withNewSsl()
                .withSslTruststoreCertificate(truststore)
                .withSslKeystoreCertificateChain(keystoreCert)
                .withSslKeystoreKey(keystoreKey)
            .endSsl()
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(4));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));

        assertThat(envVars.get(ConfigurationConstants.CA_CRT_ENV), is(truststore));
        assertThat(envVars.get(ConfigurationConstants.USER_CRT_ENV), is(keystoreCert));
        assertThat(envVars.get(ConfigurationConstants.USER_KEY_ENV), is(keystoreKey));
    }

    @Test
    void testConfiguringTracing() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";

        String tracingType = "OpenTelemetry";
        String serviceNameEnvVar = "OTEL_SERVICE_NAME";

        KafkaAdminClient kafkaAdminClient = new KafkaAdminClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withNewTracing()
                .withServiceName(name)
                .withTracingType(tracingType)
                .withServiceNameEnvVar(serviceNameEnvVar)
            .endTracing()
            .build();

        Deployment deployment = kafkaAdminClient.getDeployment();
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(3));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));

        assertThat(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
        assertThat(envVars.get(serviceNameEnvVar), is(name));
    }

    @Test
    void testBuilderThrowsExceptionsInCaseOfMissingFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder()
            .withName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder()
            .withName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder()
            .withName("client")
            .withNamespaceName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder()
            .withName("client")
            .withNamespaceName("namespace")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaAdminClientBuilder()
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        assertDoesNotThrow(() -> new KafkaAdminClientBuilder()
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("address")
            .build());
    }
}
