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

public class KafkaConsumerClientTest {

    @Test
    void testClientBuilder() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String topicName = "my-topic";
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
        String clientId = "my-client";
        String consumerGroup = "my-consumer-group";
        long messageCount = 500;
        long delayMs = 30;
        String clientRack = "rack1";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withAdditionalEnvVars(additionalEnvVars)
            .withAdditionalConfig(additionalConfig)
            .withClientId(clientId)
            .withConsumerGroup(consumerGroup)
            .withMessageCount(messageCount)
            .withDelayMs(delayMs)
            .withClientRack(clientRack)
            .build();

        Job job = kafkaConsumerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(job.getMetadata().getName(), is(name));
        assertThat(job.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(11));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.ADDITIONAL_CONFIG_ENV), is(additionalConfig));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaConsumer.name()));

        assertThat(envVars.get(ConfigurationConstants.CLIENT_ID_ENV), is(clientId));
        assertThat(envVars.get(ConfigurationConstants.GROUP_ID_ENV), is(consumerGroup));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(envVars.get(ConfigurationConstants.DELAY_MS_ENV), is(String.valueOf(delayMs)));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_RACK_ENV), is(clientRack));

        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));
    }

    @Test
    void testConfiguringImage() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String topicName = "my-topic";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        Job job = kafkaConsumerClient.getJob();
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
        String topicName = "my-topic";

        String clientId = "client-id";
        String accessToken = "my-access-token";
        String clientSecret = "client-secret";
        String refreshToken = "my-refresh-token";
        String endpointUri = "localhost:8080";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewOauth()
                .withOauthClientId(clientId)
                .withOauthAccessToken(accessToken)
                .withOauthClientSecret(clientSecret)
                .withOauthRefreshToken(refreshToken)
                .withOauthTokenEndpointUri(endpointUri)
            .endOauth()
            .build();

        Job job = kafkaConsumerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(8));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaConsumer.name()));

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
        String topicName = "my-topic";

        String jaasConfig = "jaas-config";
        String mechanism = "plain";
        String password = "tajne";
        String username = "arnost";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewSasl()
                .withSaslJaasConfig(jaasConfig)
                .withSaslMechanism(mechanism)
                .withSaslPassword(password)
                .withSaslUserName(username)
            .endSasl()
            .build();

        Job job = kafkaConsumerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(7));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaConsumer.name()));

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
        String topicName = "my-topic";

        String truststore = "truststore-certificate";
        String keystoreCert = "keystore-certificate";
        String keystoreKey = "keystore-key";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewSsl()
                .withSslTruststoreCertificate(truststore)
                .withSslKeystoreCertificateChain(keystoreCert)
                .withSslKeystoreKey(keystoreKey)
            .endSsl()
            .build();

        Job job = kafkaConsumerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(6));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaConsumer.name()));

        assertThat(envVars.get(ConfigurationConstants.CA_CRT_ENV), is(truststore));
        assertThat(envVars.get(ConfigurationConstants.USER_CRT_ENV), is(keystoreCert));
        assertThat(envVars.get(ConfigurationConstants.USER_KEY_ENV), is(keystoreKey));
    }

    @Test
    void testConfiguringTracing() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String topicName = "my-topic";

        String tracingType = "OpenTelemetry";
        String serviceNameEnvVar = "OTEL_SERVICE_NAME";

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewTracing()
                .withServiceName(name)
                .withTracingType(tracingType)
                .withServiceNameEnvVar(serviceNameEnvVar)
            .endTracing()
            .build();

        Job job = kafkaConsumerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(5));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaConsumer.name()));

        assertThat(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
        assertThat(envVars.get(serviceNameEnvVar), is(name));
    }

    @Test
    void testBuilderThrowsExceptionsInCaseOfMissingFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Topic name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Topic name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        assertDoesNotThrow(() -> new KafkaConsumerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("address")
            .build());
    }
}
