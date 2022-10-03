/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import java.util.Properties;

/**
 * Tracing initialization
 */
public interface TracingHandle {
    String type();
    String envName();
    String serviceName();
    void initialize();

    void addTracingPropsToConsumerConfig(Properties props);
    void addTracingPropsToProducerConfig(Properties props);
    void addTracingPropsToStreamsConfig(Properties props);

    <T> HttpHandle<T> createHttpHandle(String operationName);
}