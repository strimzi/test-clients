/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class HttpProducerApp {

    private static final Logger LOGGER = LogManager.getLogger(HttpProducerApp.class);

    public static void main(String[] args) throws URISyntaxException, InterruptedException, ExecutionException {
        HttpProducerConfiguration producerConfig = new HttpProducerConfiguration();
        HttpProducer producer = new HttpProducer(producerConfig);

        LOGGER.info("HTTP Producer is starting with configuration:\n{}", producerConfig.toString());

        TracingUtil.initialize();

        LOGGER.info("Sending {} messages: ", producerConfig.getMessageCount());

        System.exit(producer.sendMessages() ? 0 : 1);
    }
}
