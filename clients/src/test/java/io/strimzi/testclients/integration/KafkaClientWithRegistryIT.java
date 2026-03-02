/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.integration;

import com.google.protobuf.DynamicMessage;
import io.apicurio.registry.client.RegistryClientFactory;
import io.apicurio.registry.client.common.RegistryClientOptions;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.models.CreateArtifact;
import io.apicurio.registry.rest.client.models.CreateVersion;
import io.apicurio.registry.rest.client.models.VersionContent;
import io.apicurio.registry.rest.v3.beans.IfArtifactExists;
import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.apicurio.registry.serde.config.IdOption;
import io.apicurio.registry.serde.config.SerdeConfig;
import io.apicurio.registry.serde.protobuf.ProtobufKafkaSerializer;
import io.skodjob.datagenerator.enums.ETemplateType;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.kafka.KafkaConsumerClient;
import io.strimzi.testclients.kafka.KafkaProducerClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class KafkaClientWithRegistryIT extends io.strimzi.testclients.integration.AbstractIT {
    // Apicurio supports different storages, but the quickest is to use the official v3 image with no external dependencies.
    // We can use ENVS to specify other types. By default, the in-memory (mem) variant will be chosen.
    private static String registryImage = "quay.io/apicurio/apicurio-registry:3.1.7";
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
            ConfigurationConstants.REGISTRY_URL + "=http://0.0.0.0" + ":" + registry.getMappedPort(8080) + "/apis/registry/v2" +
            System.lineSeparator() +
            SerdeConfig.USE_ID + "=" + IdOption.contentId +
            System.lineSeparator() +
            SerdeConfig.AUTO_REGISTER_ARTIFACT + "=" + Boolean.TRUE +
            System.lineSeparator() +
            SerdeConfig.AUTO_REGISTER_ARTIFACT_IF_EXISTS + "=" + IfArtifactExists.FIND_OR_CREATE_VERSION.name();
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

    /**
     * Tests producing and registering messages with a Protobuf schema using Apicurio Schema Registry.
     *
     * <p>This test performs the following steps:
     * <ul>
     *     <li>Defines a Protobuf schema and registers it as an artifact in Apicurio Registry.</li>
     *     <li>Creates a Kafka topic with a specific configuration.</li>
     *     <li>Configures a Kafka producer to use the Protobuf serializer and the registered schema.</li>
     *     <li>Generates and sends number of Protobuf messages to the Kafka topic.</li>
     *     <li>Verifies that the produced messages are instances of {@link com.google.protobuf.DynamicMessage}.</li>
     *     <li>Checks that the expected number of messages were successfully sent by accessing internal producer state via reflection.</li>
     * </ul>
     *
     * <p>This test ensures end-to-end functionality for producing Protobuf-encoded messages
     * in Kafka while integrating with Apicurio Schema Registry.</p>
     *
     * @throws ExecutionException      if asynchronous producer execution fails
     * @throws InterruptedException    if the producer thread is interrupted
     * @throws NoSuchFieldException    if internal reflection access fails
     * @throws IllegalAccessException  if internal reflection access fails
     * @throws TimeoutException        if message production exceeds the allowed timeout
     */
    @Test
    void testExchangeProtobuf() throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, TimeoutException {

        final String topicName = "test-protobuf";
        final String artifactId = topicName + "-value";
        final int messageCount = 20;
        final String message = "{\"name\":\"Bob\",\"age\":30}";
        final String protoSchema = """
            syntax = "proto3";
            message Person {
                string name = 1;
                int32 age = 2;
            }
            """;

        // Protobuf needs to register schema unlike other types
        RegistryClientOptions options = RegistryClientOptions.create("http://localhost:" + registry.getMappedPort(8080));
        RegistryClient client = RegistryClientFactory.create(options);

        CreateArtifact createArtifact = new CreateArtifact();
        createArtifact.setArtifactId(artifactId);
        createArtifact.setArtifactType("PROTOBUF");

        VersionContent content = new VersionContent();
        content.setContent(protoSchema);
        content.setContentType("application/x-protobuf");

        CreateVersion createVersion = new CreateVersion();
        createVersion.setContent(content);
        createArtifact.setFirstVersion(createVersion);

        client.groups().byGroupId(ConfigurationConstants.DEFAULT_GROUP_ID).artifacts().post(createArtifact);
        // Verify presence of the artifact
        assertEquals(artifactId, client.groups().byGroupId(ConfigurationConstants.DEFAULT_GROUP_ID).artifacts().byArtifactId(artifactId).get().getArtifactId());

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, kafkaCluster.getBootstrapServers());
        configuration.put(ConfigurationConstants.TOPIC_ENV, topicName);
        configuration.put(ConfigurationConstants.MESSAGE_ENV, message);
        configuration.put(ConfigurationConstants.MESSAGE_COUNT_ENV, "20");

        // Registry config for PROTOBUF
        configuration.put(ConfigurationConstants.ADDITIONAL_CONFIG_ENV,
            ConfigurationConstants.REGISTRY_ARTIFACT_ID + "=" + artifactId +
            System.lineSeparator() +
            ConfigurationConstants.REGISTRY_API_VERSION + "=" + ConfigurationConstants.APICURIO_API_V2 +
            System.lineSeparator() +
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG + "=" + ProtobufKafkaSerializer.class.getName() +
            getRegistryConfig()
        );

        createKafkaTopic(topicName, Map.of(TopicConfig.RETENTION_MS_CONFIG, "604800000"));

        KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(configuration);

        // Explicitly check the record message type
        ProducerRecord record = kafkaProducerClient.generateMessage(1);
        assertInstanceOf(DynamicMessage.class, record.value());

        CompletableFuture<Void> future = CompletableFuture.runAsync(kafkaProducerClient::run);

        future.get(10, TimeUnit.SECONDS);

        Field producedMessages = KafkaProducerClient.class.getDeclaredField("messageSuccessfullySent");
        producedMessages.setAccessible(true);

        assertThat(producedMessages.get(kafkaProducerClient), is(messageCount));
    }
}
