/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.streams.KafkaClientSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tracing initialization
 */
public class TracingUtil {
    private static final Logger log = LogManager.getLogger(TracingUtil.class);
    private static TracingHandle tracing = new NoopTracing();

    private static final List<TracingHandle> HANDLES = List.of(
        new OpenTracingHandle(),
        new OpenTelemetryHandle()
    );

    public static TracingHandle getTracing() {
        return tracing;
    }

    public static TracingHandle initialize() {
        for (TracingHandle instance : HANDLES) {
            String serviceName = instance.serviceName();
            if (serviceName != null) {
                log.info(
                    String.format(
                        "Initializing Jaeger (%s) tracingConfig with service name %s",
                        instance.type(),
                        serviceName
                    )
                );
                instance.initialize();
                tracing = instance;
                break;
            }
        }
        return tracing;
    }

    private static final class NoopTracing implements TracingHandle {
        @Override
        public String type() {
            return null;
        }

        @Override
        public String envName() {
            return null;
        }

        @Override
        public String serviceName() {
            return null;
        }

        @Override
        public void initialize() {
        }

        @Override
        public void kafkaConsumerConfig(Properties props) {
        }

        @Override
        public void kafkaProducerConfig(Properties props) {
        }

        @Override
        public KafkaClientSupplier clientSupplier() {
            return null;
        }

        @Override
        public <T> HttpHandle<T> createHttpHandle(String operationName) {
            return new HttpHandle<T>();
        }
    }

}