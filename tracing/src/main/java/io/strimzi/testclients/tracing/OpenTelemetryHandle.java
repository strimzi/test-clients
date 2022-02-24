/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.kafkaclients.KafkaTracing;
import io.opentelemetry.instrumentation.kafkaclients.TracingConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.TracingProducerInterceptor;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaClientSupplier;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;

public class OpenTelemetryHandle implements TracingHandle {
    private static final String OPEN_TELEMERTY = "OpenTelemetry";
    private static final String OTEL_SERVICE_NAME = "OTEL_SERVICE_NAME";
    private static final String OTEL_SERVICE_NAME_KEY = "otel.service.name";
    private static final String OTEL_TRACES_EXPORTER = "OTEL_TRACES_EXPORTER";
    private static final String OTEL_TRACES_EXPORTER_KEY = "otel.traces.exporter";
    private static final String JAEGER = "jaeger";
    private static final String TEST_CLIENTS = "test-clients";

    @Override
    public String type() {
        return OPEN_TELEMERTY;
    }

    @Override
    public String envName() {
        return OTEL_SERVICE_NAME;
    }

    @Override
    public String serviceName() {
        String serviceName = System.getenv(envName());
        if (serviceName == null) {
            serviceName = System.getProperty(OTEL_SERVICE_NAME_KEY);
        } else {
            System.setProperty(OTEL_SERVICE_NAME_KEY, serviceName);
        }
        if (serviceName != null && System.getenv(OTEL_TRACES_EXPORTER) == null) {
            System.setProperty(OTEL_TRACES_EXPORTER_KEY, JAEGER);
        }
        return serviceName;
    }

    @Override
    public void initialize() {
        AutoConfiguredOpenTelemetrySdk.initialize();
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

    @Override
    public <T> HttpHandle<T> createHttpHandle(String operationName) {
        return new OpenTelemetryHttpHandle<>(operationName);
    }

    private static class TracingKafkaClientSupplier implements KafkaClientSupplier {
        @Override
        public Admin getAdmin(Map<String, Object> config) {
            return Admin.create(config);
        }

        @Override
        public Producer<byte[], byte[]> getProducer(Map<String, Object> config) {
            KafkaTracing tracing = KafkaTracing.create(GlobalOpenTelemetry.get());
            return tracing.wrap(new KafkaProducer<>(config));
        }

        @Override
        public Consumer<byte[], byte[]> getConsumer(Map<String, Object> config) {
            KafkaTracing tracing = KafkaTracing.create(GlobalOpenTelemetry.get());
            return tracing.wrap(new KafkaConsumer<>(config));
        }

        @Override
        public Consumer<byte[], byte[]> getRestoreConsumer(Map<String, Object> config) {
            return getConsumer(config);
        }

        @Override
        public Consumer<byte[], byte[]> getGlobalConsumer(Map<String, Object> config) {
            return getConsumer(config);
        }
    }

    private static class OpenTelemetryHttpHandle<T> extends HttpHandle<T> {
        private final String operationName;
        private Span span;

        public OpenTelemetryHttpHandle(String operationName) {
            this.operationName = operationName;
        }

        private static Tracer get() {
            return GlobalOpenTelemetry.getTracer(TEST_CLIENTS);
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