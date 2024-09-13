/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.properties;

import io.strimzi.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.configuration.kafka.KafkaProducerConfiguration;
import io.strimzi.configuration.kafka.KafkaStreamsConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.configuration.ConfigurationConstants.ADDITIONAL_CONFIG_ENV;
import static io.strimzi.configuration.ConfigurationConstants.APPLICATION_ID_ENV;
import static io.strimzi.configuration.ConfigurationConstants.BOOTSTRAP_SERVERS_ENV;
import static io.strimzi.configuration.ConfigurationConstants.CLIENT_ID_ENV;
import static io.strimzi.configuration.ConfigurationConstants.CLIENT_RACK_ENV;
import static io.strimzi.configuration.ConfigurationConstants.COMMIT_INTERVAL_MS_ENV;
import static io.strimzi.configuration.ConfigurationConstants.DEFAULT_CLIENT_ID;
import static io.strimzi.configuration.ConfigurationConstants.DEFAULT_COMMIT_INTERVAL_MS;
import static io.strimzi.configuration.ConfigurationConstants.DEFAULT_GROUP_ID;
import static io.strimzi.configuration.ConfigurationConstants.DEFAULT_PRODUCER_ACKS;
import static io.strimzi.configuration.ConfigurationConstants.GROUP_ID_ENV;
import static io.strimzi.configuration.ConfigurationConstants.PRODUCER_ACKS_ENV;
import static io.strimzi.configuration.ConfigurationConstants.SOURCE_TOPIC_ENV;
import static io.strimzi.configuration.ConfigurationConstants.TARGET_TOPIC_ENV;
import static io.strimzi.configuration.ConfigurationConstants.TOPIC_ENV;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class KafkaPropertiesTest {

    @Test
    void testConfigureProducerProperties() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");

        KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        // check default properties
        Properties producerProperties = KafkaProperties.producerProperties(kafkaProducerConfiguration);

        assertThat(producerProperties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), is(StringSerializer.class.getName()));
        assertThat(producerProperties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG), is(StringSerializer.class.getName()));
        assertThat(producerProperties.getProperty(ProducerConfig.ACKS_CONFIG), is(DEFAULT_PRODUCER_ACKS));

        String producerAcks = "0";
        configuration.put(PRODUCER_ACKS_ENV, producerAcks);
        configuration.put(ADDITIONAL_CONFIG_ENV, "value.serializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer\n" +
            "key.serializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer");

        kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
        producerProperties = KafkaProperties.producerProperties(kafkaProducerConfiguration);

        // check custom properties
        assertThat(producerProperties.getProperty(ProducerConfig.ACKS_CONFIG), is(producerAcks));
        assertThat(producerProperties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaSerializer"));
        assertThat(producerProperties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaSerializer"));
    }

    @Test
    void testConfigureConsumerProperties() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(TOPIC_ENV, "my-topic");

        KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        // check default properties
        Properties consumerProperties = KafkaProperties.consumerProperties(kafkaConsumerConfiguration);

        assertThat(consumerProperties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG), is(StringDeserializer.class.getName()));
        assertThat(consumerProperties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG), is(StringDeserializer.class.getName()));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG), is(DEFAULT_CLIENT_ID));
        assertThat(consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), is(DEFAULT_GROUP_ID));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_RACK_CONFIG), nullValue());
        assertThat(consumerProperties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), is("earliest"));
        assertThat(consumerProperties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), is("false"));

        String clientId = "random-client-id";
        String groupId = "random-group-id";
        String clientRack = "rack-0";

        configuration.put(CLIENT_ID_ENV, clientId);
        configuration.put(GROUP_ID_ENV, groupId);
        configuration.put(CLIENT_RACK_ENV, clientRack);
        configuration.put(ADDITIONAL_CONFIG_ENV, "value.deserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer\n" +
            "key.deserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer");

        kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);

        consumerProperties = KafkaProperties.consumerProperties(kafkaConsumerConfiguration);

        // check custom properties
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG), is(clientId));
        assertThat(consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), is(groupId));
        assertThat(consumerProperties.getProperty(ConsumerConfig.CLIENT_RACK_CONFIG), is(clientRack));
        assertThat(consumerProperties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaDeserializer"));
        assertThat(consumerProperties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaDeserializer"));
    }

    @Test
    void testConfigureStreamsProperties() {
        String appId = "my-app-0";
        Map<String, String> configuration = new HashMap<>();
        configuration.put(BOOTSTRAP_SERVERS_ENV, "my-cluster-kafka:9092");
        configuration.put(SOURCE_TOPIC_ENV, "source-my-topic");
        configuration.put(TARGET_TOPIC_ENV, "target-my-topic");
        configuration.put(APPLICATION_ID_ENV, appId);

        KafkaStreamsConfiguration kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        // check default properties
        Properties streamsProperties = KafkaProperties.streamsProperties(kafkaStreamsConfiguration);

        assertThat(streamsProperties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG), is(appId));
        assertThat(streamsProperties.get(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG), is(DEFAULT_COMMIT_INTERVAL_MS));
        assertThat(streamsProperties.get(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG).toString(), is(Serdes.String().getClass().toString()));
        assertThat(streamsProperties.get(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG).toString(), is(Serdes.String().getClass().toString()));

        long commitInterval = 7000L;
        configuration.put(COMMIT_INTERVAL_MS_ENV, String.valueOf(commitInterval));
        configuration.put(ADDITIONAL_CONFIG_ENV, "default.value.serde=io.apicurio.registry.serde.avro.AvroKafkaDeserializer\n" +
            "default.key.serde=io.apicurio.registry.serde.avro.AvroKafkaDeserializer");

        kafkaStreamsConfiguration = new KafkaStreamsConfiguration(configuration);

        streamsProperties = KafkaProperties.streamsProperties(kafkaStreamsConfiguration);

        // check custom properties
        assertThat(streamsProperties.get(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG), is(commitInterval));
        assertThat(streamsProperties.getProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaDeserializer"));
        assertThat(streamsProperties.getProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG),
            is("io.apicurio.registry.serde.avro.AvroKafkaDeserializer"));
    }
}
