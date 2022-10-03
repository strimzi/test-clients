/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Tracing related utilization class
 */
public class TracingUtil {
    private static final Logger LOGGER = LogManager.getLogger(TracingUtil.class);
    private static TracingHandle tracing = new NoopTracing();

    /**
     * Method for getting instance of tracing handler
     * @return TracingHandle instance of one of the handlers - OpenTracingHandle, OpenTelemetryHandle, NoopTracing
     */
    public static TracingHandle getTracing() {
        return tracing;
    }

    /**
     * Method for initializing tracing based on `TRACING_TYPE` env variable
     * @return TracingHandle instance of one of the handlers - OpenTracingHandle, OpenTelemetryHandle, NoopTracing
     */
    public static TracingHandle initialize() {
        String tracingSystem = System.getenv("TRACING_TYPE");

        switch (tracingSystem == null ? "none" : tracingSystem) {
            case TracingConstants.OPEN_TRACING:
                tracing = new OpenTracingHandle();
                break;
            case TracingConstants.OPEN_TELEMERTY:
                tracing = new OpenTelemetryHandle();
                break;
            default:
                tracing = new NoopTracing();
                break;
        }

        if (!(tracing instanceof NoopTracing)) {
            LOGGER.info("Initializing {} with service name: {}", tracing.getType(), tracing.getServiceName());
            tracing.initialize();
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
        public String getType() {
            return null;
        }

        @Override
        public String getServiceName() {
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
            return new HttpHandle<>();
        }
    }

}