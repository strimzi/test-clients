/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.kafkaclients.TracingConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.TracingProducerInterceptor;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaClientSupplier;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class OpenTelemetryHandle implements TracingHandle {
    @Override
    public String getType() {
        return TracingConstants.OPEN_TELEMERTY;
    }

    @Override
    public String getServiceName() {
        String serviceName = System.getenv(TracingConstants.OTEL_SERVICE_NAME_ENV);
        if (serviceName == null) {
            serviceName = System.getProperty(TracingConstants.OTEL_SERVICE_NAME_KEY);
        } else {
            System.setProperty(TracingConstants.OTEL_SERVICE_NAME_KEY, serviceName);
        }
        if (serviceName != null && System.getenv(TracingConstants.OTEL_TRACES_EXPORTER_ENV) == null) {
            System.setProperty(TracingConstants.OTEL_TRACES_EXPORTER_KEY, TracingConstants.JAEGER);
        }
        return serviceName;
    }

    @Override
    public void initialize() {
        // disable metrics
        System.setProperty("otel.metrics.exporter", "none");
        AutoConfiguredOpenTelemetrySdk.initialize();
    }

    @Override
    public void addTracingPropsToConsumerConfig(Properties props) {
        TracingUtil.addProperty(props, ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
    }

    @Override
    public void addTracingPropsToProducerConfig(Properties props) {
        TracingUtil.addProperty(props, ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
    }

    @Override
    public KafkaStreams getStreamsWithTracing(Topology topology, Properties props) {
        KafkaClientSupplier supplier = new TracingKafkaClientSupplier();
        return new KafkaStreams(topology, props, supplier);
    }

    @Override
    public <T> HttpHandle<T> createHttpHandle(String operationName) {
        return new OpenTelemetryHttpHandle<>(operationName);
    }

    private static class OpenTelemetryHttpHandle<T> extends HttpHandle<T> {
        private final String operationName;
        private Span span;

        public OpenTelemetryHttpHandle(String operationName) {
            this.operationName = operationName;
        }

        private static Tracer get() {
            return GlobalOpenTelemetry.getTracer(TracingConstants.TEST_CLIENTS);
        }

        private static TextMapPropagator propagator() {
            return GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        }

        @Override
        public HttpRequest build(HttpContext context) {
            SpanBuilder spanBuilder = get().spanBuilder(operationName);
            spanBuilder.setSpanKind(SpanKind.CLIENT);
            spanBuilder.setAttribute(SemanticAttributes.HTTP_METHOD, context.getRecord() == null ? "GET" : "POST");
            spanBuilder.setAttribute(SemanticAttributes.HTTP_URL, context.getUri());
            span = spanBuilder.startSpan();
            HttpRequest.Builder builder = builder(context);
            try (Scope ignored = span.makeCurrent()) {
                propagator().inject(Context.current(), builder, HttpRequest.Builder::setHeader);
            }
            return builder.build();
        }

        @Override
        public HttpResponse<T> finish(HttpResponse<T> response) {
            try (Scope ignored = span.makeCurrent()) {
                int code = response.statusCode();
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, code);
                span.setStatus(code == 200 ? StatusCode.OK : StatusCode.ERROR);
            } finally {
                span.end();
            }
            return response;
        }
    }
}