/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.Properties;

import io.strimzi.test.tracing.TracingUtil;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        StreamsConfiguration config = new StreamsConfiguration();

        LOGGER.info("Kafka streams is starting with configuration: {}", config.toString());

        Properties props = StreamsConfiguration.createProperties(config);

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(config.getSourceTopic(), Consumed.with(Serdes.String(), Serdes.String()))
            .mapValues(value -> {
                StringBuilder sb = new StringBuilder();
                sb.append(value);
                return sb.reverse().toString();
            })
            .to(config.getTargetTopic(), Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = builder.build();

        KafkaStreams streams = TracingUtil.initialize().getStreamsWithTracing(topology, props);

        streams.start();
    }
}
