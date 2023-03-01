/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi;

import io.strimzi.common.ClientType;
import io.strimzi.common.ClientsInterface;
import io.strimzi.common.configuration.Constants;
import io.strimzi.http.consumer.HttpConsumerClient;
import io.strimzi.http.producer.HttpProducerClient;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        TracingUtil.initialize();
        Map<String, String> envConfiguration = System.getenv();
        ClientType clientType = ClientType.getFromString(envConfiguration.get(Constants.CLIENT_TYPE_ENV));

        ClientsInterface client = null;

        switch (clientType) {
            case HttpProducer:
                client = new HttpProducerClient(envConfiguration);
                break;
            case HttpConsumer:
                client = new HttpConsumerClient(envConfiguration);
                break;
            default:
                LOGGER.error("Unknown client type specified, exiting");
                System.exit(-1);
        }

        client.run();
    }
}