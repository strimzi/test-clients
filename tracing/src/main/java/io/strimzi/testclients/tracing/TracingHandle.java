/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.tracing;

import org.apache.kafka.streams.KafkaClientSupplier;

import java.util.Properties;

/**
 * Tracing handle interface
 */
public interface TracingHandle {
    String getType();
    String getServiceName();
    void initialize();

    void addTracingPropsToConsumerConfig(Properties props);
    void addTracingPropsToProducerConfig(Properties props);

    KafkaClientSupplier getStreamsClientSupplier();

    <T> HttpHandle<T> createHttpHandle(String operationName);
}