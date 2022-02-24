/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.kafka.consumer;

import io.strimzi.testclients.tracing.TracingUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerApp {
    private static final Logger LOGGER = LogManager.getLogger(KafkaConsumerApp.class);

    public static void main(String[] args) {
        ConsumerConfiguration config = new ConsumerConfiguration();
        start(config);
    }

    public static void start(ConsumerConfiguration config) {
        LOGGER.info("Kafka consumer is starting with configuration: {}", config.toString());

        Properties props = ConsumerConfiguration.createProperties(config);
        int receivedMsgs = 0;

        TracingUtil.initialize().kafkaConsumerConfig(props);

        boolean commit = !Boolean.parseBoolean(config.getEnableAutoCommit());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(config.getTopic()));

        while (receivedMsgs < config.getMessageCount()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            for (ConsumerRecord<String, String> record : records) {
                LOGGER.info("Received message:");
                LOGGER.info("\tpartition: {}", record.partition());
                LOGGER.info("\toffset: {}", record.offset());
                LOGGER.info("\tvalue: {}", record.value());
                if (record.headers() != null) {
                    LOGGER.info("\theaders: ");
                    for (Header header : record.headers()) {
                        LOGGER.info("\t\tkey: {}, value: {}", header.key(), new String(header.value()));
                    }
                }
                receivedMsgs++;
            }
            if (commit) {
                consumer.commitSync();
            }
        }
        LOGGER.info("Received {} messages", receivedMsgs);
    }
}
