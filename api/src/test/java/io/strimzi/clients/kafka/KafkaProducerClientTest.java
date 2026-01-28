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

public class KafkaProducerClientTest {
    
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
        long messageCount = 500;
        long delayMs = 30;

        String acks = "0";
        String message = "my hello message";
        String messageKey = "my-key";
        String messageTemplate = "template:x";
        String headers = "my-headers";

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withAdditionalEnvVars(additionalEnvVars)
            .withAdditionalConfig(additionalConfig)
            .withMessageCount(messageCount)
            .withDelayMs(delayMs)
            .withAcks(acks)
            .withMessage(message)
            .withMessageKey(messageKey)
            .withMessageTemplate(messageTemplate)
            .withHeaders(headers)
            .build();

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(job.getMetadata().getName(), is(name));
        assertThat(job.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(13));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.ADDITIONAL_CONFIG_ENV), is(additionalConfig));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

        assertThat(envVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(envVars.get(ConfigurationConstants.DELAY_MS_ENV), is(String.valueOf(delayMs)));
        assertThat(envVars.get(ConfigurationConstants.PRODUCER_ACKS_ENV), is(acks));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_ENV), is(message));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_KEY_ENV), is(messageKey));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_TEMPLATE_ENV), is(messageTemplate));
        assertThat(envVars.get(ConfigurationConstants.HEADERS_ENV), is(headers));

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

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
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

        Job job = kafkaProducerClient.getJob();
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

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
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

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(8));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

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

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
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

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(7));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

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

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
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

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(6));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

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

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
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

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(5));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

        assertThat(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
        assertThat(envVars.get(serviceNameEnvVar), is(name));
    }

    @Test
    void testConfiguringTransactional() {
        String name = "client";
        String namespaceName = "my-namespace";
        String bootstrapAddress = "localhost:9092";
        String topicName = "my-topic";

        Long messagesPerTransaction = 3L;

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withBootstrapAddress(bootstrapAddress)
            .withTopicName(topicName)
            .withNewTransactional()
                .withMessagesPerTransaction(messagesPerTransaction)
            .endTransactional()
            .build();

        Job job = kafkaProducerClient.getJob();
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        // this will ensure that no other env variables are set, only those we are setting
        assertThat(envVars.size(), is(4));
        assertThat(envVars.get(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), is(bootstrapAddress));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_TYPE_ENV), is(ClientType.KafkaProducer.name()));

        assertThat(envVars.get(ConfigurationConstants.MESSAGES_PER_TRANSACTION_ENV), is(String.valueOf(messagesPerTransaction)));
    }

    @Test
    void testBuilderThrowsExceptionsInCaseOfMissingFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaProducerClientBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Topic name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaProducerClientBuilder()
            .withTopicName("my-topic")
            .build()
        );
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaProducerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new KafkaProducerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Bootstrap address cannot be empty"));

        assertDoesNotThrow(() -> new KafkaProducerClientBuilder()
            .withTopicName("my-topic")
            .withName("client")
            .withNamespaceName("namespace")
            .withBootstrapAddress("address")
            .build());
    }
}
