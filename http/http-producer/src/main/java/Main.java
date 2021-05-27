/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        HttpProducerConfiguration producerConfig = new HttpProducerConfiguration();

        LOGGER.info("HTTP Producer is starting with configuration:\n{}", producerConfig.toString());

        if (System.getenv("JAEGER_SERVICE_NAME") != null)   {
            Tracer tracer = Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);
        }
    }
}
