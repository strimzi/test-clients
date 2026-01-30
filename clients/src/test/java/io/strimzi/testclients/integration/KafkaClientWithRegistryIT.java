/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.integration;

import io.apicurio.registry.rest.v2.beans.IfExists;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.apicurio.registry.serde.config.IdOption;
import io.skodjob.datagenerator.enums.ETemplateType;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.kafka.KafkaConsumerClient;
import io.strimzi.testclients.kafka.KafkaProducerClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class KafkaClientWithRegistryIT extends io.strimzi.testclients.integration.AbstractIT {
    private static String registryImage = "quay.io/apicurio/apicurio-registry-mem:2.6.5.Final";
    private static GenericContainer registry;

    @BeforeAll
    void startRegistry() {
        registry = new GenericContainer<>(DockerImageName.parse(registryImage))
            .withNetwork(Network.SHARED)
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/apis/registry/v2/system/info")
                .forStatusCode(200));
        registry.start();
    }

    @AfterAll
    void stopRegistry() {
        registry.stop();
    }

    private String getRegistryConfig() {
        return System.lineSeparator() +
            "apicurio.registry.url=http://0.0.0.0" + ":" + registry.getMappedPort(8080) + "/apis/registry/v2" +
            System.lineSeparator() +
            SerdeConfig.USE_ID + "=" + IdOption.contentId +
            System.lineSeparator() +
            SerdeConfig.AUTO_REGISTER_ARTIFACT + "=" + Boolean.TRUE +
            System.lineSeparator() +
            SerdeConfig.AUTO_REGISTER_ARTIFACT_IF_EXISTS + "=" + IfExists.RETURN.name();
    }

    private Stream<Arguments> getImplementedTemplates() {
        return Stream.of(
            Arguments.of(ETemplateType.PAYROLL.name()),
            Arguments.of(ETemplateType.IOT_DEVICE.name()),
            Arguments.of(ETemplateType.STARGATE.name()),
            Arguments.of(ETemplateType.STARWARS.name()),
            Arguments.of(ETemplateType.PAYMENT_FIAT.name()),
            Arguments.of(ETemplateType.FLIGHTS.name())
        );
    }

    @ParameterizedTest
    @MethodSource("getImplementedTemplates")
    void testExchangeAvro(String templateName) throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, TimeoutException {
        String topicName = templateName.toLowerCase(Locale.ROOT);
        Map<String, String> configuration;
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");
        configuration.put(ConfigurationConstants.MESSAGE_TEMPLATE_ENV, templateName);
        // Registry config
        configuration.put("ADDITIONAL_CONFIG", ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG + "=" + AvroKafkaSerializer.class.getName() +
            getRegistryConfig());

        createKafkaTopic(topicName, Map.of(TopicConfig.RETENTION_MS_CONFIG, "604800000"));

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);

        CompletableFuture<Void> future = CompletableFuture.runAsync(kafkaProducerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field producedMessages = KafkaProducerClient.class.getDeclaredField("messageSuccessfullySent");
        producedMessages.setAccessible(true);
        assertThat(producedMessages.get(kafkaProducerClient), is(100));

        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");
        configuration.put(ConfigurationConstants.GROUP_ID_ENV, topicName);
        configuration.put(ConfigurationConstants.CLIENT_ID_ENV, topicName);

        // Registry config
        configuration.put("ADDITIONAL_CONFIG", ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + "=" + AvroKafkaDeserializer.class.getName() +
            getRegistryConfig());

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(configuration);

        future = CompletableFuture.runAsync(kafkaConsumerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(15, TimeUnit.SECONDS);

        Field consumedMessages = KafkaConsumerClient.class.getDeclaredField("consumedMessages");
        consumedMessages.setAccessible(true);
        assertThat(consumedMessages.get(kafkaConsumerClient), is(100));
    }
}
