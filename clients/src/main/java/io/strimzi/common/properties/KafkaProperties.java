/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.properties;

import io.strimzi.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.configuration.kafka.KafkaProducerConfiguration;
import io.strimzi.configuration.kafka.KafkaStreamsConfiguration;
import io.strimzi.properties.BasicKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaProperties {
    public static Properties producerProperties(KafkaProducerConfiguration configuration) {
        Properties properties = BasicKafkaProperties.clientProperties(configuration);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, configuration.getAcks());

        return properties;
    }

    public static Properties consumerProperties(KafkaConsumerConfiguration configuration) {
        Properties properties = BasicKafkaProperties.clientProperties(configuration);

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, configuration.getClientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.getGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
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
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return properties;
    }
}
