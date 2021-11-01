/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import java.util.Properties;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.contrib.kafka.streams.TracingKafkaClientSupplier;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaClientSupplier;

public class OpenTracingHandle implements TracingHandle {
    @Override
    public String type() {
        return "OpenTracing";
    }

    @Override
    public String envName() {
        return Configuration.JAEGER_SERVICE_NAME;
    }

    @Override
    public String serviceName() {
        return System.getenv(envName());
    }

    @Override
    public void initialize() {
        Tracer tracer = Configuration.fromEnv().getTracer();
        GlobalTracer.registerIfAbsent(tracer);
    }

    @Override
    public void kafkaConsumerConfig(Properties props) {
        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
    }

    @Override
    public void kafkaProducerConfig(Properties props) {
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
    }

    @Override
    public KafkaClientSupplier clientSupplier() {
        return new TracingKafkaClientSupplier();
    }
}