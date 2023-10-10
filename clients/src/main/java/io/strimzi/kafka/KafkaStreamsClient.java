/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka;

import io.strimzi.common.ClientsInterface;
import io.strimzi.configuration.kafka.KafkaStreamsConfiguration;
import io.strimzi.common.properties.KafkaProperties;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaClientSupplier;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.internals.DefaultKafkaClientSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;

public class KafkaStreamsClient implements ClientsInterface {
    private static final Logger LOGGER = LogManager.getLogger(KafkaStreamsClient.class);
    private final KafkaStreamsConfiguration configuration;
    private final Properties properties;

    public KafkaStreamsClient(Map<String, String> configuration) {
        this.configuration = new KafkaStreamsConfiguration(configuration);
        this.properties = KafkaProperties.streamsProperties(this.configuration);
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), this.configuration.toString());

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(configuration.getSourceTopic(), Consumed.with(Serdes.String(), Serdes.String()))
            .mapValues(value -> {
                StringBuilder sb = new StringBuilder();
                sb.append(value);
                return sb.reverse().toString();
            })
            .to(configuration.getTargetTopic(), Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = builder.build();
        KafkaClientSupplier clientSupplier = new DefaultKafkaClientSupplier();

        if (this.configuration.isTracingEnabled()) {
            clientSupplier = TracingUtil.getTracing().getStreamsClientSupplier();
        }

        KafkaStreams streams = new KafkaStreams(topology, this.properties, clientSupplier);

        streams.start();
    }

    @Override
    public void awaitCompletion() {

    }

    @Override
    public void checkFinalState() {

    }
}
