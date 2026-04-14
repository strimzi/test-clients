/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.http;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.Image;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpConsumerClientTest {

    @Test
    void testConsumerBuilderCreatesCorrectJob() {
        String name = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        int messageCount = 300;
        int pollInterval = 10;
        String topicName = "my-topic";
        String endpointPrefix = "prefix";
        String sslTruststoreCert = "truststore";
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
        String clientId = "my-client";
        String consumerGroup = "my-consumer-group";
        int pollTimeout = 1;

        HttpConsumerClient httpConsumerClient = new HttpConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withPollInterval(pollInterval)
            .withTopicName(topicName)
            .withEndpointPrefix(endpointPrefix)
            .withSslTruststoreCertificate(sslTruststoreCert)
            .withAdditionalEnvVars(additionalEnvVars)
            .withClientId(clientId)
            .withConsumerGroup(consumerGroup)
            .withPollTimeout(pollTimeout)
            .build();

        Job httpConsumerJob = httpConsumerClient.getJob();
        Container container = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> envVars = container.getEnv().stream()
            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        Map<String, EnvVarSource> envVarsWithValueFrom = container.getEnv().stream()
            .filter(e -> e.getValueFrom() != null)
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValueFrom));

        assertThat(httpConsumerJob.getMetadata().getName(), is(name));
        assertThat(httpConsumerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        assertThat(envVars.get(ConfigurationConstants.HOSTNAME_ENV), is(hostName));
        assertThat(envVars.get(ConfigurationConstants.PORT_ENV), is(String.valueOf(port)));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(envVars.get(ConfigurationConstants.POLL_INTERVAL_ENV), is(String.valueOf(pollInterval)));
        assertThat(envVars.get(ConfigurationConstants.POLL_TIMEOUT_ENV), is(String.valueOf(pollTimeout)));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), is(endpointPrefix));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_ID_ENV), is(clientId));
        assertThat(envVars.get(ConfigurationConstants.GROUP_ID_ENV), is(consumerGroup));

        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));

        // these should not exist
        assertNull(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV));

        assertThat(envVarsWithValueFrom.get(ConfigurationConstants.CA_CRT_ENV).getSecretKeyRef().getName(), is(sslTruststoreCert));

        assertThat(httpConsumerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of()));
    }

    @Test
    void testConfigureTracing() {
        String name = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        int messageCount = 300;
        int pollInterval = 10;
        String topicName = "my-topic";
        String serviceNameEnvVar = "OTEL_SERVICE_NAME";
        String tracingType = "OpenTelemetry";
        EnvVar additionalTracingEnv = new EnvVarBuilder()
            .withName("OTEL_EXPORTER_OTLP_ENDPOINT")
            .withValue("endpoint")
            .build();

        HttpConsumerClient httpConsumerClient = new HttpConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withPollInterval(pollInterval)
            .withTopicName(topicName)
            .withNewTracing()
                .withServiceNameEnvVar(serviceNameEnvVar)
                .withServiceName(name)
                .withTracingType(tracingType)
                .withAdditionalTracingEnvVars(additionalTracingEnv)
            .endTracing()
            .build();

        Job httpConsumerJob = httpConsumerClient.getJob();
        Map<String, String> consumerEnvVars = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0)
            .getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(consumerEnvVars.get(serviceNameEnvVar), is(name));
        assertThat(consumerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
        assertThat(consumerEnvVars.get("OTEL_EXPORTER_OTLP_ENDPOINT"), is("endpoint"));
    }

    @Test
    void testConfigureCustomImageConfig() {
        String name = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        int messageCount = 300;
        int pollInterval = 10;
        String topicName = "my-topic";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        HttpConsumerClient httpConsumerClient = new HttpConsumerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withPollInterval(pollInterval)
            .withTopicName(topicName)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        Job httpConsumerJob = httpConsumerClient.getJob();
        Container container = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(httpConsumerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));
    }

    @Test
    void testEmptyBuilderThrowsExceptionsForImportantFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of the client cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Hostname cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Hostname cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Port cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Topic cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .withTopicName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Topic cannot be empty"));

        assertDoesNotThrow(() -> new HttpConsumerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .withTopicName("my-topic")
            .build());
    }
}
