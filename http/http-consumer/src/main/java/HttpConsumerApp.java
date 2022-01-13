/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class HttpConsumerApp {

    private static final Logger LOGGER = LogManager.getLogger(HttpConsumerApp.class);

    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException, IOException {
        HttpConsumerConfiguration consumerConfig = new HttpConsumerConfiguration();
        HttpConsumer consumer = new HttpConsumer(consumerConfig);

        LOGGER.info("HttpConsumer is starting with configuration: {}", consumerConfig.toString());

        TracingUtil.initialize();

        consumer.createConsumer();
        consumer.subscribeToTopic();
        System.exit(consumer.consumeMessages() ? 0 : 1);
    }
}
