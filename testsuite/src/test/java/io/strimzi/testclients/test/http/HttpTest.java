/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.http;

import io.strimzi.test.container.StrimziKafkaContainer;
import io.strimzi.testclients.http.consumer.HttpConsumerApp;
import io.strimzi.testclients.http.consumer.HttpConsumerConfiguration;
import io.strimzi.testclients.http.producer.HttpProducerApp;
import io.strimzi.testclients.http.producer.HttpProducerConfiguration;
import io.strimzi.testclients.test.support.KafkaBridgeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startable;

@Testcontainers
public class HttpTest {

    private static final String TOPIC = "mytopic";

    @Container
    JoinedContainer container = new JoinedContainer();

    @Test
    public void testBridge() throws Exception {
        HttpProducerConfiguration producerConfig = new HttpProducerConfiguration(
            container.getBridgeHost(), container.getBridgePort(), TOPIC
        );
        HttpProducerApp.start(producerConfig, false);

        HttpConsumerConfiguration consumerConfig = new HttpConsumerConfiguration(
            container.getBridgeHost(), container.getBridgePort(), TOPIC
        );
        HttpConsumerApp.start(consumerConfig, false);
    }

    // Order matters
    private static class JoinedContainer implements Startable {
        StrimziKafkaContainer kafkaContainer = new StrimziKafkaContainer();
        KafkaBridgeContainer bridgeContainer = new KafkaBridgeContainer(() -> kafkaContainer.getBootstrapServers());


        String getBridgeHost() {
            return bridgeContainer.getHost();
        }

        String getBridgePort() {
            return String.valueOf(bridgeContainer.getEmbeddedHttpServerPort());
        }

        @Override
        public void start() {
            kafkaContainer.start();
            bridgeContainer.start();
        }

        @Override
        public void stop() {
            bridgeContainer.stop();
            kafkaContainer.stop();
        }
    }
}
