/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import java.util.Properties;

import org.apache.kafka.streams.KafkaClientSupplier;

/**
 * Tracing initialization
 */
public interface TracingHandle {
    String type();
    String envName();
    String serviceName();
    void initialize();

    void kafkaConsumerConfig(Properties props);
    void kafkaProducerConfig(Properties props);

    KafkaClientSupplier clientSupplier();
}