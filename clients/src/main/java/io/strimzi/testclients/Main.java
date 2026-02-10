/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients;

import io.strimzi.testclients.common.ClientsInterface;
import io.strimzi.testclients.configuration.ClientType;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.http.consumer.HttpConsumerClient;
import io.strimzi.testclients.http.producer.HttpProducerClient;
import io.strimzi.testclients.kafka.KafkaConsumerClient;
import io.strimzi.testclients.kafka.KafkaProducerClient;
import io.strimzi.testclients.kafka.KafkaStreamsClient;
import io.strimzi.testclients.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        TracingUtil.initialize();
        Map<String, String> envConfiguration = System.getenv();
        ClientType clientType = ClientType.getFromString(envConfiguration.get(ConfigurationConstants.CLIENT_TYPE_ENV));

        ClientsInterface client = null;

        switch (clientType) {
            case HttpProducer:
                client = new HttpProducerClient(envConfiguration);
                break;
            case HttpConsumer:
                client = new HttpConsumerClient(envConfiguration);
                break;
            case KafkaProducer:
                client = new KafkaProducerClient(envConfiguration);
                break;
            case KafkaConsumer:
                client = new KafkaConsumerClient(envConfiguration);
                break;
            case KafkaStreams:
                client = new KafkaStreamsClient(envConfiguration);
                break;
            default:
                LOGGER.error("Unknown client type specified, exiting");
                System.exit(-1);
        }

        client.run();
    }
}