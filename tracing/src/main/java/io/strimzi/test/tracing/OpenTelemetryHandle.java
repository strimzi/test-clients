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
import io.opentelemetry.instrumentation.kafkaclients.KafkaTracing;
import io.opentelemetry.instrumentation.kafkaclients.TracingConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.TracingProducerInterceptor;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
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
    @Override
    public String type() {
        return "OpenTelemetry";
    }

    @Override
    public String envName() {
        return "OTEL_SERVICE_NAME";
    }

    @Override
    public String serviceName() {
        String serviceName = System.getenv(envName());
        if (serviceName == null) {
            serviceName = System.getProperty("otel.service.name");
        } else {
            System.setProperty("otel.service.name", serviceName);
        }
        if (serviceName != null && System.getenv("OTEL_TRACES_EXPORTER") == null) {
            System.setProperty("otel.traces.exporter", "jaeger");
        }
        return serviceName;
    }

    @Override
    public void initialize() {
        OpenTelemetrySdkAutoConfiguration.initialize();
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
        private Scope scope;

        public OpenTelemetryHttpHandle(String operationName) {
            this.operationName = operationName;
        }

        private static Tracer get() {
            return GlobalOpenTelemetry.getTracer("test-clients");
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
            scope = span.makeCurrent();
            HttpRequest.Builder builder = builder(context);
            propagator().inject(Context.current(), builder, HttpRequest.Builder::setHeader);
            return builder.build();
        }

        @Override
        public HttpResponse<T> finish(HttpResponse<T> response) {
            try {
                int code = response.statusCode();
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, code);
                span.setStatus(code == 200 ? StatusCode.OK : StatusCode.ERROR);
                scope.close();
            } finally {
                span.end();
            }
            return response;
        }
    }
}