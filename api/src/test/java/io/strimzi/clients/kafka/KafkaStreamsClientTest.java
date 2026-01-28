/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.configuration.ClientType;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.configuration.Image;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KafkaStreamsClientTest {
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

        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";
        long commitIntervalMs = 300;

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withAdditionalEnvVars(additionalEnvVars)
            .withAdditionalConfig(additionalConfig)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withCommitIntervalMs(commitIntervalMs)
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(job.getMetadata().getName(), is(name));
        assertThat(job.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(9));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.ADDITIONAL_CONFIG_ENV), is(additionalConfig));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaStreams.name()));

        assertThat(envVars.get(ConfigurationConstants.APPLICATION_ID_ENV), is(applicationId));
        assertThat(envVars.get(ConfigurationConstants.SOURCE_TOPIC_ENV), is(sourceTopicName));
        assertThat(envVars.get(ConfigurationConstants.TARGET_TOPIC_ENV), is(targetTopicName));
        assertThat(envVars.get(ConfigurationConstants.COMMIT_INTERVAL_MS_ENV), is(String.valueOf(commitIntervalMs)));

        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));
    }

    @Test
    void testConfiguringImage() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(job.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));
    }

    @Test
    void testConfiguringOAuth() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";

        String clientId = "client-id";
        String accessToken = "my-access-token";
        String clientSecret = "client-secret";
        String refreshToken = "my-refresh-token";
        String endpointUri = "localhost:8080";

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withNewOauth()
                .withOauthClientId(clientId)
                .withOauthAccessToken(accessToken)
                .withOauthClientSecret(clientSecret)
                .withOauthRefreshToken(refreshToken)
                .withOauthTokenEndpointUri(endpointUri)
            .endOauth()
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(10));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaStreams.name()));
        assertThat(envVars.get(ConfigurationConstants.APPLICATION_ID_ENV), is(applicationId));
        assertThat(envVars.get(ConfigurationConstants.SOURCE_TOPIC_ENV), is(sourceTopicName));
        assertThat(envVars.get(ConfigurationConstants.TARGET_TOPIC_ENV), is(targetTopicName));

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
        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";

        String jaasConfig = "jaas-config";
        String mechanism = "plain";
        String password = "tajne";
        String username = "arnost";

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withNewSasl()
                .withSaslJaasConfig(jaasConfig)
                .withSaslMechanism(mechanism)
                .withSaslPassword(password)
                .withSaslUserName(username)
            .endSasl()
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(9));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaStreams.name()));
        assertThat(envVars.get(ConfigurationConstants.APPLICATION_ID_ENV), is(applicationId));
        assertThat(envVars.get(ConfigurationConstants.SOURCE_TOPIC_ENV), is(sourceTopicName));
        assertThat(envVars.get(ConfigurationConstants.TARGET_TOPIC_ENV), is(targetTopicName));

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
        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";

        String truststore = "truststore-certificate";
        String keystoreCert = "keystore-certificate";
        String keystoreKey = "keystore-key";

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withNewSsl()
                .withSslTruststoreCertificate(truststore)
                .withSslKeystoreCertificateChain(keystoreCert)
                .withSslKeystoreKey(keystoreKey)
            .endSsl()
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(8));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaStreams.name()));
        assertThat(envVars.get(ConfigurationConstants.APPLICATION_ID_ENV), is(applicationId));
        assertThat(envVars.get(ConfigurationConstants.SOURCE_TOPIC_ENV), is(sourceTopicName));
        assertThat(envVars.get(ConfigurationConstants.TARGET_TOPIC_ENV), is(targetTopicName));

        assertThat(envVars.get(ConfigurationConstants.CA_CRT_ENV), is(truststore));
        assertThat(envVars.get(ConfigurationConstants.USER_CRT_ENV), is(keystoreCert));
        assertThat(envVars.get(ConfigurationConstants.USER_KEY_ENV), is(keystoreKey));
    }

    @Test
    void testConfiguringTracing() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String applicationId = "my-application";
        String sourceTopicName = "source-topic";
        String targetTopicName = "target-topic";

        String tracingType = "OpenTelemetry";
        String serviceNameEnvVar = "OTEL_SERVICE_NAME";

        KafkaStreamsClient kafkaStreamsClient = new KafkaStreamsClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withApplicationId(applicationId)
            .withSourceTopicName(sourceTopicName)
            .withTargetTopicName(targetTopicName)
            .withNewTracing()
                .withServiceName(name)
                .withTracingType(tracingType)
                .withServiceNameEnvVar(serviceNameEnvVar)
            .endTracing()
            .build();

        Job job = kafkaStreamsClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(7));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaStreams.name()));
        assertThat(envVars.get(ConfigurationConstants.APPLICATION_ID_ENV), is(applicationId));
        assertThat(envVars.get(ConfigurationConstants.SOURCE_TOPIC_ENV), is(sourceTopicName));
        assertThat(envVars.get(ConfigurationConstants.TARGET_TOPIC_ENV), is(targetTopicName));

        assertThat(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
        assertThat(envVars.get(serviceNameEnvVar), is(name));
    }

    @Test
    void testBuilderThrowsExceptionsInCaseOfMissingFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Application ID cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Source topic name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .withSourceTopicName("source-topic")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Target topic name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .withSourceTopicName("source-topic")
            .withTargetTopicName("target-topic")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .withSourceTopicName("source-topic")
            .withTargetTopicName("target-topic")
            .withName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .withSourceTopicName("source-topic")
            .withTargetTopicName("target-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        assertDoesNotThrow(() -> new KafkaStreamsClientBuilder()
            .withApplicationId("my-application")
            .withSourceTopicName("source-topic")
            .withTargetTopicName("target-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("address")
            .build());
    }
}
