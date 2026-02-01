/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.common.properties;

import io.strimzi.testclients.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.testclients.configuration.kafka.KafkaProducerConfiguration;
import io.strimzi.testclients.configuration.kafka.KafkaStreamsConfiguration;
import io.strimzi.testclients.properties.BasicKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaProperties {
    public static Properties producerProperties(KafkaProducerConfiguration configuration) {
        Properties properties = BasicKafkaProperties.clientProperties(configuration);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configuration.getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configuration.getValueSerializer());
        properties.put(ProducerConfig.ACKS_CONFIG, configuration.getAcks());

        return properties;
    }

    public static Properties consumerProperties(KafkaConsumerConfiguration configuration) {
        Properties properties = BasicKafkaProperties.clientProperties(configuration);

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, configuration.getClientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.getGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configuration.getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configuration.getValueDeserializer());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        if (configuration.getClientRack() != null) {
            properties.put(ConsumerConfig.CLIENT_RACK_CONFIG, configuration.getClientRack());
        }

        return properties;
    }

    public static Properties streamsProperties(KafkaStreamsConfiguration configuration) {
        Properties properties = BasicKafkaProperties.clientProperties(configuration);

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, configuration.getApplicationId());
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, configuration.getCommitIntervalMs());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, configuration.getDefaultKeySerde());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, configuration.getDefaultValueSerde());

        return properties;
    }
}
