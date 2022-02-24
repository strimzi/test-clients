/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.tracing;

import io.strimzi.test.container.StrimziKafkaContainer;
import io.strimzi.testclients.http.consumer.HttpConsumerApp;
import io.strimzi.testclients.http.consumer.HttpConsumerConfiguration;
import io.strimzi.testclients.http.producer.HttpProducerApp;
import io.strimzi.testclients.http.producer.HttpProducerConfiguration;
import io.strimzi.testclients.test.support.JaegerContainer;
import io.strimzi.testclients.test.support.KafkaBridgeContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Testcontainers
public class TracingTest {
    private static final Logger log = LoggerFactory.getLogger(TracingTest.class);

    private static final String TOPIC = "mytracing";

    @Container
    JoinedContainer container = new JoinedContainer();

    @Test
    public void testTracing() throws Exception {
        HttpProducerConfiguration producerConfig = new HttpProducerConfiguration(
            container.getBridgeHost(), container.getBridgePort(), TOPIC
        );
        HttpProducerApp.start(producerConfig, false);

        HttpConsumerConfiguration consumerConfig = new HttpConsumerConfiguration(
            container.getBridgeHost(), container.getBridgePort(), TOPIC
        );
        HttpConsumerApp.start(consumerConfig, false);

        Thread.sleep(30_000L);

        HttpClient client = HttpClient.newHttpClient();

        String servicesUrl = String.format(
            "http://%s:%s/api/services",
            container.getJaegerHost(), container.getJaegerQueryPort()
        );
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(servicesUrl))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        log.info("Services: {}", response.body());

        String tracesUrl = String.format(
            "http://%s:%s/api/traces?service=%s",
            container.getJaegerHost(), container.getJaegerQueryPort(), "mybridge"
        );
        request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(tracesUrl))
            .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        log.info("Traces: {}", response.body());
    }

    // Order matters
    private static class JoinedContainer implements Startable {
        StrimziKafkaContainer kafkaContainer = new StrimziKafkaContainer();
        JaegerContainer jaegerContainer;
        KafkaBridgeContainer bridgeContainer;

        String getBridgeHost() {
            return bridgeContainer.getHost();
        }

        String getBridgePort() {
            return String.valueOf(bridgeContainer.getEmbeddedHttpServerPort());
        }

        String getJaegerHost() {
            return jaegerContainer.getHost();
        }

        String getJaegerUdpPort() {
            return String.valueOf(jaegerContainer.getMappedPort(JaegerContainer.JAEGER_UDP_PORT));
        }

        String getJaegerGrpcPort() {
            return String.valueOf(jaegerContainer.getMappedPort(JaegerContainer.JAEGER_COLLECTOR_GRPC_PORT));
        }

        String getJaegerQueryPort() {
            return String.valueOf(jaegerContainer.getMappedPort(JaegerContainer.JAEGER_QUERY_PORT));
        }

        @Override
        public void start() {
            String tracing = System.getProperty("tracing");
            if (tracing == null) {
                throw new IllegalStateException("Missing -Dtracing system property");
            }

            kafkaContainer.start();

            jaegerContainer = new JaegerContainer();
            jaegerContainer.start();

            Map<String, String> envs = new HashMap<>();
            if ("jaeger".equals(tracing)) {
                envs.put("JAEGER_SERVICE_NAME", "mybridge");
                envs.put(
                    "JAEGER_AGENT_HOST",
                    String.format("http://%s:%s", getJaegerHost(), getJaegerUdpPort())
                );
            } else if ("opentelemetry".equals(tracing)) {
                envs.put("OTEL_SERVICE_NAME", "mybridge");
                envs.put(
                    "OTEL_EXPORTER_JAEGER_ENDPOINT",
                    String.format("http://%s:%s", getJaegerHost(), getJaegerGrpcPort())
                );
            } else {
                throw new IllegalArgumentException("Invalid -Dtracing value: " + tracing);
            }

            log.info("Envs: {}", envs);

            bridgeContainer = new KafkaBridgeContainer(() -> kafkaContainer.getBootstrapServers())
                .withTracing(tracing)
                .withEnv(envs);
            bridgeContainer.start();
        }

        @Override
        public void stop() {
            bridgeContainer.stop();
            jaegerContainer.stop();
            kafkaContainer.stop();
        }
    }
}
