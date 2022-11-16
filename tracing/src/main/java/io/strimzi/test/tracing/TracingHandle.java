/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

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

    KafkaStreams getStreamsWithTracing(Topology topology, Properties props);

    <T> HttpHandle<T> createHttpHandle(String operationName);
}