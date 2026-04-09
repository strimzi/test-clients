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

public class HttpProducerConsumerTest {

    @Test
    void testProducerConsumerBuilderCreatesCorrectJobs() {
        String producerName = "my-producer";
        String consumerName = "my-consumer";
        String namespaceName = "my-namespace";
        String hostName = "localhost";
        int port = 8080;
        int messageCount = 300;
        String message = "hello there";
        int delayMs = 10;
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
        int pollTimeout = 1;

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
        Map<String, String> envVars = container.getEnv().stream()
            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        Map<String, EnvVarSource> envVarsWithValueFrom = container.getEnv().stream()
            .filter(e -> e.getValueFrom() != null)
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValueFrom));

        assertThat(httpProducerJob.getMetadata().getName(), is(producerName));
        assertThat(httpProducerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(producerName));
        assertThat(container.getImage(), is(Image.defaultImage));

        assertThat(envVars.get(ConfigurationConstants.HOSTNAME_ENV), is(hostName));
        assertThat(envVars.get(ConfigurationConstants.PORT_ENV), is(String.valueOf(port)));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_ENV), is(message));
        assertThat(envVars.get(ConfigurationConstants.DELAY_MS_ENV), is(String.valueOf(delayMs)));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), is(endpointPrefix));
        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));

        assertThat(envVarsWithValueFrom.get(ConfigurationConstants.CA_CRT_ENV).getSecretKeyRef().getName(), is(sslTruststoreCert));

        // these should not exist
        assertNull(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV));
        assertThat(httpProducerJob.getSpec().getTemplate().getSpec().getImagePullSecrets(), is(List.of()));

        // Consumer part
        Job httpConsumerJob = httpProducerConsumer.getConsumer().getJob();
        container = httpConsumerJob.getSpec().getTemplate().getSpec().getContainers().get(0);
        envVars = container.getEnv().stream()
            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

        envVarsWithValueFrom = container.getEnv().stream()
            .filter(e -> e.getValueFrom() != null)
            .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValueFrom));

        assertThat(httpConsumerJob.getMetadata().getName(), is(consumerName));
        assertThat(httpConsumerJob.getMetadata().getNamespace(), is(namespaceName));

        assertThat(container.getName(), is(consumerName));
        assertThat(container.getImage(), is(Image.defaultImage));

        assertThat(envVars.get(ConfigurationConstants.HOSTNAME_ENV), is(hostName));
        assertThat(envVars.get(ConfigurationConstants.PORT_ENV), is(String.valueOf(port)));
        assertThat(envVars.get(ConfigurationConstants.MESSAGE_COUNT_ENV), is(String.valueOf(messageCount)));
        assertThat(envVars.get(ConfigurationConstants.POLL_INTERVAL_ENV), is(String.valueOf(delayMs)));
        assertThat(envVars.get(ConfigurationConstants.POLL_TIMEOUT_ENV), is(String.valueOf(pollTimeout)));
        assertThat(envVars.get(ConfigurationConstants.TOPIC_ENV), is(topicName));
        assertThat(envVars.get(ConfigurationConstants.ENDPOINT_PREFIX_ENV), is(endpointPrefix));
        assertThat(envVars.get(ConfigurationConstants.CLIENT_ID_ENV), is(clientId));
        assertThat(envVars.get(ConfigurationConstants.GROUP_ID_ENV), is(consumerGroup));

        assertThat(envVars.get("RANDOM"), is("value"));
        assertThat(envVars.get("SOME"), is("thing"));

        assertThat(envVarsWithValueFrom.get(ConfigurationConstants.CA_CRT_ENV).getSecretKeyRef().getName(), is(sslTruststoreCert));

        // these should not exist
        assertNull(envVars.get(ConfigurationConstants.TRACING_TYPE_ENV));
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
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> new HttpProducerConsumerBuilder()
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
