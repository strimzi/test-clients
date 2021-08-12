/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
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

        if (System.getenv("JAEGER_SERVICE_NAME") != null)   {
            Tracer tracer = Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);
        }

        LOGGER.info("Sending {} messages: ", producerConfig.getMessageCount());

        System.exit(producer.sendMessages() ? 0 : 1);
    }
}
