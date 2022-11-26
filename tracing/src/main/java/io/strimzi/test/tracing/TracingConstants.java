/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

public final class TracingConstants {
    /**
     * Tracing type
     */
    public static final String OPEN_TELEMERTY = "OpenTelemetry";
    public static final String OPEN_TRACING = "OpenTracing";

    /**
     * OpenTelemetry related constants
     */
    public static final String OTEL_SERVICE_NAME_ENV = "OTEL_SERVICE_NAME";
    public static final String OTEL_SERVICE_NAME_KEY = "otel.service.name";
    public static final String OTEL_TRACES_EXPORTER_ENV = "OTEL_TRACES_EXPORTER";
    public static final String OTEL_TRACES_EXPORTER_KEY = "otel.traces.exporter";
    public static final String TEST_CLIENTS = "test-clients";

    /**
     * Exporter related constants
     */
    public static final String OTLP_EXPORTER = "otlp";
}
