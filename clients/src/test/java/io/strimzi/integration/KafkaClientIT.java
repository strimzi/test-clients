/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.integration;

import io.skodjob.datagenerator.enums.ETemplateType;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.kafka.KafkaConsumerClient;
import io.strimzi.kafka.KafkaProducerClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaClientIT extends AbstractIT {

    @Test
    void testSimpleExchange() throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, TimeoutException {
        Map<String, String> configuration;
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");

        createKafkaTopic("my-topic", Map.of(TopicConfig.RETENTION_MS_CONFIG, "604800000"));

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);

        CompletableFuture<Void> future = CompletableFuture.runAsync(kafkaProducerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field producedMessages = KafkaProducerClient.class.getDeclaredField("messageSuccessfullySent");
        producedMessages.setAccessible(true);
        assertThat(producedMessages.get(kafkaProducerClient), is(100));

        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, "my-topic");
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(configuration);

        future = CompletableFuture.runAsync(kafkaConsumerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field consumedMessages = KafkaConsumerClient.class.getDeclaredField("consumedMessages");
        consumedMessages.setAccessible(true);
        assertThat(consumedMessages.get(kafkaConsumerClient), is(100));
    }

    @ParameterizedTest
    @MethodSource("getImplementedTemplates")
    void testUseTemplateWithoutRegistry(String templateName, String serializer, String deserializer) throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, TimeoutException {
        String topicName = (templateName + serializer).toLowerCase(Locale.ROOT);
        Map<String, String> configuration;
        configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "100");
        configuration.put(ConfigurationConstants.MESSAGE_TEMPLATE_ENV, templateName);
        configuration.put("ADDITIONAL_CONFIG", ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG + "=" + serializer);

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
        configuration.put("ADDITIONAL_CONFIG", ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + "=" + deserializer);

        KafkaConsumerClient kafkaConsumerClient = new KafkaConsumerClient(configuration);

        future = CompletableFuture.runAsync(kafkaConsumerClient::run);
        // Wait for the process to complete within a reasonable time
        future.get(10, TimeUnit.SECONDS);

        Field consumedMessages = KafkaConsumerClient.class.getDeclaredField("consumedMessages");
        consumedMessages.setAccessible(true);
        assertThat(consumedMessages.get(kafkaConsumerClient), is(100));
    }

    private Stream<Arguments> getImplementedTemplates() {
        return Stream.of(
            Arguments.of(ETemplateType.PAYROLL.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.IOT_DEVICE.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.STARGATE.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.STARWARS.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.PAYMENT_FIAT.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.FLIGHTS.name(), StringSerializer.class.getName(), StringDeserializer.class.getName()),
            Arguments.of(ETemplateType.PAYROLL.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName()),
            Arguments.of(ETemplateType.IOT_DEVICE.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName()),
            Arguments.of(ETemplateType.STARGATE.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName()),
            Arguments.of(ETemplateType.STARWARS.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName()),
            Arguments.of(ETemplateType.PAYMENT_FIAT.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName()),
            Arguments.of(ETemplateType.FLIGHTS.name(), ByteArraySerializer.class.getName(), ByteArrayDeserializer.class.getName())
        );
    }
}
