/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import io.jaegertracing.Configuration;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaClientSupplier;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

@Deprecated
public class OpenTracingHandle implements TracingHandle {
    @Override
    public String getType() {
        return TracingConstants.OPEN_TRACING;
    }

    @Override
    public String getServiceName() {
        return System.getenv(Configuration.JAEGER_SERVICE_NAME);
    }

    @Override
    public void initialize() {
        Tracer tracer = Configuration.fromEnv().getTracer();
        GlobalTracer.registerIfAbsent(tracer);
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
    // KafkaStreams is not working with OpenTracing from Kafka 3.0.0
    public KafkaClientSupplier getStreamsClientSupplier() {
        return null;
    }

    @Override
    public <T> HttpHandle<T> createHttpHandle(String operationName) {
        return new OpenTracingHttpHandle<>(operationName);
    }

    private static class OpenTracingHttpHandle<T> extends HttpHandle<T> {
        private final String operationName;
        private Span span;

        public OpenTracingHttpHandle(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public HttpRequest build(HttpContext context) {
            Tracer tracer = GlobalTracer.get();
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName);
            span = spanBuilder.start();
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(span, context.getRecord() == null ? "GET" : "POST");
            Tags.HTTP_URL.set(span, context.getUri());
            HttpRequest.Builder builder = builder(context);
            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    throw new UnsupportedOperationException("carrier is write-only");
                }

                @Override
                public void put(String key, String value) {
                    builder.setHeader(key, value);
                }
            });
            return builder.build();
        }

        @Override
        public HttpResponse<T> finish(HttpResponse<T> response) {
            Tags.HTTP_STATUS.set(span, response.statusCode());
            span.finish();
            return response;
        }
    }
}