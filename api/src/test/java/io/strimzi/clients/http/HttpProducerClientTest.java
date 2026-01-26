/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.http;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.configuration.Image;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpProducerClientTest {

    @Test
    void testProducerBuilderCreatesCorrectJob() {
        String name = "my-producer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        long messageCount = 300L;
        String message = "hello there";
        long delayMs = 10L;
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

        HttpProducerClient httpProducerClient = new HttpProducerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withMessage(message)
            .withDelayMs(delayMs)
            .withTopicName(topicName)
            .withEndpointPrefix(endpointPrefix)
            .withSslTruststoreCertificate(sslTruststoreCert)
            .withAdditionalEnvVars(additionalEnvVars)
            .build();

        Job httpProducerJob = httpProducerClient.getJob();
        Container container = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> producerEnvVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(httpProducerJob.getMetadata().getName(), is(name));
        assertThat(httpProducerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(name));
        assertThat(container.getImage(), is(Image.defaultImage));

        assertThat(producerEnvVars.get(ConfigurationConstants.HOSTNAME_ENV), is(hostName));
        assertThat(producerEnvVars.get(ConfigurationConstants.PORT_ENV), is(String.valueOf(port)));
        assertThat(producerEnvVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(producerEnvVars.get(ConfigurationConstants.MESSAGE_ENV), is(message));
        assertThat(producerEnvVars.get(ConfigurationConstants.DELAY_MS_ENV), is(String.valueOf(delayMs)));
        assertThat(producerEnvVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(producerEnvVars.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), is(endpointPrefix));
        assertThat(producerEnvVars.get(ConfigurationConstants.CA_CRT_ENV), is(sslTruststoreCert));
        assertThat(producerEnvVars.get("RANDOM"), is("value"));
        assertThat(producerEnvVars.get("SOME"), is("thing"));

        // these should not exist
        assertNull(producerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV));
        assertThat(httpProducerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of()));
    }

    @Test
    void testConfigureTracing() {
        String name = "my-producer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        long messageCount = 300L;
        long delayMs = 10L;
        String topicName = "my-topic";
        String serviceNameEnvVar = "OTEL_SERVICE_NAME";
        String tracingType = "OpenTelemetry";

        HttpProducerClient httpProducerClient = new HttpProducerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withDelayMs(delayMs)
            .withTopicName(topicName)
            .withNewTracing()
                .withServiceNameEnvVar(serviceNameEnvVar)
                .withServiceName(name)
                .withTracingType(tracingType)
            .endTracing()
            .build();

        Job httpProducerJob = httpProducerClient.getJob();
        Map<String, String> producerEnvVars = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0)
            .getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(producerEnvVars.get(serviceNameEnvVar), is(name));
        assertThat(producerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
    }

    @Test
    void testConfigureCustomImageConfig() {
        String name = "my-producer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        long messageCount = 300L;
        long delayMs = 10L;
        String topicName = "my-topic";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        HttpProducerClient httpProducerClient = new HttpProducerClientBuilder()
            .withName(name)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessageCount(messageCount)
            .withDelayMs(delayMs)
            .withTopicName(topicName)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        Job httpProducerJob = httpProducerClient.getJob();
        Container container = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(httpProducerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));
    }

    @Test
    void testEmptyBuilderThrowsExceptionsForImportantFields() {
        assertThrows(IllegalArgumentException.class, () -> new HttpProducerClientBuilder().build(), "Name of the client cannot be empty");
        assertThrows(IllegalArgumentException.class, () -> new HttpProducerClientBuilder()
            .withName("client")
            .build(), "Name of Namespace cannot be empty");
        assertThrows(IllegalArgumentException.class, () -> new HttpProducerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .build(), "Hostname cannot be empty");
        assertThrows(IllegalArgumentException.class, () -> new HttpProducerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .build(), "Port cannot be empty");
        assertThrows(IllegalArgumentException.class, () -> new HttpProducerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .build(), "Name of Topic cannot be empty");
        assertDoesNotThrow(() -> new HttpProducerClientBuilder()
            .withName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .withTopicName("my-topic")
            .build());
    }
}
