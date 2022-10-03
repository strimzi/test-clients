/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Tracing initialization
 */
public class TracingUtil {
    private static final Logger LOGGER = LogManager.getLogger(TracingUtil.class);
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
                LOGGER.info(
                    "Initializing {} tracingConfig with service name {}",
                    instance.type(),
                    serviceName
                );
                instance.initialize();
                tracing = instance;
                break;
            }
        }
        return tracing;
    }

    static void addProperty(Properties props, String key, String value) {
        String previous = props.getProperty(key);
        if (previous != null) {
            props.setProperty(key, previous + "," + value);
        } else {
            props.setProperty(key, value);
        }
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
        public void addTracingPropsToConsumerConfig(Properties props) {
        }

        @Override
        public void addTracingPropsToProducerConfig(Properties props) {
        }

        @Override
        public void addTracingPropsToStreamsConfig(Properties props) {
        }

        @Override
        public <T> HttpHandle<T> createHttpHandle(String operationName) {
            return new HttpHandle<T>();
        }
    }

}