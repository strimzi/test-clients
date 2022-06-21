/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.http.producer;

import io.strimzi.testclients.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class HttpProducerApp {

    private static final Logger LOGGER = LogManager.getLogger(HttpProducerApp.class);

    public static void main(String[] args) throws URISyntaxException, InterruptedException, ExecutionException {
        HttpProducerConfiguration producerConfig = new HttpProducerConfiguration();
        start(producerConfig, true);
    }

    public static void start(HttpProducerConfiguration producerConfig, boolean exit) throws URISyntaxException, InterruptedException, ExecutionException {
        LOGGER.info("HTTP Producer is starting with configuration:\n{}", producerConfig.toString());

        HttpProducer producer = new HttpProducer(producerConfig);

        TracingUtil.initialize();

        LOGGER.info("Sending {} messages: ", producerConfig.getMessageCount());

        boolean sent = producer.sendMessages();
        if (exit) {
            System.exit(sent ? 0 : 1);
        }
    }
}
