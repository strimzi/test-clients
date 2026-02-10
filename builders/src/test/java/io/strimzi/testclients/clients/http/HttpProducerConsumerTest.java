/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.http;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
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

public class HttpProducerConsumerTest {

    @Test
    void testProducerConsumerBuilderCreatesCorrectJobs() {
        String producerName = "my-producer";
        String consumerName = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        long messageCount = 300L;
        String message = "hello there";
        long delayMs = 10L;
        String topicName = "my-topic";
        String endpointPrefix = "prefix";
        String sslTruststoreCert = "truststore";
        String consumerGroup = "my-consumer-group";
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
        Long pollTimeout = 1L;

        HttpProducerConsumer httpProducerConsumer = new HttpProducerConsumerBuilder()
            .withProducerName(producerName)
            .withConsumerName(consumerName)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withMessage(message)
            .withMessageCount(messageCount)
            .withConsumerGroup(consumerGroup)
            .withClientId(clientId)
            .withPollTimeout(pollTimeout)
            .withDelayMs(delayMs)
            .withAdditionalEnvVars(additionalEnvVars)
            .withTopicName(topicName)
            .withEndpointPrefix(endpointPrefix)
            .withSslTruststoreCertificate(sslTruststoreCert)
            .build();

        // Producer part
        Job httpProducerJob = httpProducerConsumer.getProducer().getJob();
        Container container = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> producerEnvVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(httpProducerJob.getMetadata().getName(), is(producerName));
        assertThat(httpProducerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(producerName));
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

        // Consumer part
        Job httpConsumerJob = httpProducerConsumer.getConsumer().getJob();
        container = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0);
        Map<String, String> consumerEnvVars = container.getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(httpConsumerJob.getMetadata().getName(), is(consumerName));
        assertThat(httpConsumerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(consumerName));
        assertThat(container.getImage(), is(Image.defaultImage));

        assertThat(consumerEnvVars.get(ConfigurationConstants.HOSTNAME_ENV), is(hostName));
        assertThat(consumerEnvVars.get(ConfigurationConstants.PORT_ENV), is(String.valueOf(port)));
        assertThat(consumerEnvVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(consumerEnvVars.get(ConfigurationConstants.POLL_INTERVAL_ENV), is(String.valueOf(delayMs)));
        assertThat(consumerEnvVars.get(ConfigurationConstants.POLL_TIMEOUT_ENV), is(String.valueOf(pollTimeout)));
        assertThat(consumerEnvVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(consumerEnvVars.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), is(endpointPrefix));
        assertThat(consumerEnvVars.get(ConfigurationConstants.CA_CRT_ENV), is(sslTruststoreCert));
        assertThat(consumerEnvVars.get(ConfigurationConstants.CLIENT_ID_ENV), is(clientId));
        assertThat(consumerEnvVars.get(ConfigurationConstants.GROUP_ID_ENV), is(consumerGroup));

        assertThat(consumerEnvVars.get("RANDOM"), is("value"));
        assertThat(consumerEnvVars.get("SOME"), is("thing"));

        // these should not exist
        assertNull(consumerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV));
        assertThat(httpConsumerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of()));
    }

    @Test
    void testConfigureTracing() {
        String producerName = "my-producer";
        String consumerName = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        String topicName = "my-topic";

        String serviceNameEnvVar = "OTEL_SERVICE_NAME";
        String tracingType = "OpenTelemetry";
        String tracingServiceName = "tracing";
        String producerServiceName = tracingServiceName + "-producer";
        String consumerServiceName = tracingServiceName + "-consumer";

        HttpProducerConsumer httpProducerConsumer = new HttpProducerConsumerBuilder()
            .withProducerName(producerName)
            .withConsumerName(consumerName)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withTopicName(topicName)
            .withNewTracing()
                .withServiceName(tracingServiceName)
                .withServiceNameEnvVar(serviceNameEnvVar)
                .withTracingType(tracingType)
            .endTracing()
            .build();

        // Producer part
        Job httpProducerJob = httpProducerConsumer.getProducer().getJob();
        Map<String, String> producerEnvVars = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0)
            .getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(producerEnvVars.get(serviceNameEnvVar), is(producerServiceName));
        assertThat(producerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));


        // Consumer part
        Job httpConsumerJob = httpProducerConsumer.getConsumer().getJob();
        Map<String, String> consumerEnvVars = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0)
            .getEnv().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        assertThat(consumerEnvVars.get(serviceNameEnvVar), is(consumerServiceName));
        assertThat(consumerEnvVars.get(ConfigurationConstants.TRACING_TYPE_ENV), is(tracingType));
    }

    @Test
    void testConfigureCustomImageConfig() {
        String producerName = "my-producer";
        String consumerName = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        String topicName = "my-topic";

        String imageName = "my-custom.registry.io/org/repo:latest";
        String pullPolicy = "Always";
        String pullSecret = "topsecret";

        HttpProducerConsumer httpProducerConsumer = new HttpProducerConsumerBuilder()
            .withProducerName(producerName)
            .withConsumerName(consumerName)
            .withNamespaceName(namespaceName)
            .withHostname(hostName)
            .withPort(port)
            .withTopicName(topicName)
            .withNewImage()
                .withImageName(imageName)
                .withImagePullPolicy(pullPolicy)
                .withImagePullSecret(pullSecret)
            .endImage()
            .build();

        // Producer part
        Job httpProducerJob = httpProducerConsumer.getProducer().getJob();
        Container container = httpProducerJob.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(httpProducerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));

        // Consumer part
        Job httpConsumerJob = httpProducerConsumer.getConsumer().getJob();
        container = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertThat(container.getImage(), is(imageName));
        assertThat(container.getImagePullPolicy(), is(pullPolicy));
        assertThat(httpConsumerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of(new LocalObjectReference(pullSecret))));
    }

    @Test
    void testEmptyBuilderThrowsExceptionsForImportantFields() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder().build());
        assertThat(illegalArgumentException.getMessage(), is("Producer name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Producer name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Consumer name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Consumer name cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Namespace cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Hostname cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .withHostname("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Hostname cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Port cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Topic cannot be empty"));

        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .withTopicName("")
            .build());
        assertThat(illegalArgumentException.getMessage(), is("Name of Topic cannot be empty"));

        assertDoesNotThrow(() -> new HttpProducerConsumerBuilder()
            .withProducerName("client")
            .withConsumerName("client")
            .withNamespaceName("myproject")
            .withHostname("localhost")
            .withPort(8080)
            .withTopicName("my-topic")
            .build());
    }
}
