/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.kafka.producer;

import io.strimzi.testclients.tracing.TracingUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaProducerApp {
    private static final Logger LOGGER = LogManager.getLogger(KafkaProducerApp.class);

    public static void main(String[] args) throws InterruptedException {
        ProducerConfiguration config = new ProducerConfiguration();
        start(config, true);
    }

    public static void start(ProducerConfiguration config, boolean exit) throws InterruptedException {
        LOGGER.info("Kafka producer is starting with configuration: {}", config.toString());

        Properties props = ProducerConfiguration.createProperties(config);
        List<Header> headers = null;

        TracingUtil.initialize().kafkaProducerConfig(props);

        if (config.getHeaders() != null) {
            headers = new ArrayList<>();

            String[] headersArray = config.getHeaders().split(", [\t\n\r]?");
            for (String header : headersArray) {
                headers.add(new RecordHeader(header.split("=")[0], header.split("=")[1].getBytes()));
            }
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        LOGGER.info("Sending {} messages ...", config.getMessageCount());

        boolean blockProducer = System.getenv("BLOCKING_PRODUCER") != null;
        boolean transactionalProducer = config.getAdditionalConfig().contains("transactional.id");
        int msgPerTx = Integer.parseInt(System.getenv().getOrDefault("MESSAGES_PER_TRANSACTION", "10"));
        if (transactionalProducer) {
            LOGGER.info("Using transactional producer. Initializing the transactions ...");
            producer.initTransactions();
        }
        AtomicLong numSent = new AtomicLong(0);
        for (long i = 0; i < config.getMessageCount(); i++) {
            if (transactionalProducer && i % msgPerTx == 0) {
                LOGGER.info("Beginning new transaction. Messages sent: {}", i);
                producer.beginTransaction();
            }
            LOGGER.info("Sending messages \"" + config.getMessage() + " - {}\"{}", i, config.getHeaders() == null ? "" : " - with headers - " + config.getHeaders());
            Future<RecordMetadata> recordMetadataFuture = producer.send(new ProducerRecord<>(config.getTopic(), null, null, null, "\"" + config.getMessage() + " - " + i + "\"", headers));
            if (blockProducer) {
                try {
                    recordMetadataFuture.get();
                    // Increment number of sent messages only if ack is received by producer
                    numSent.incrementAndGet();
                } catch (ExecutionException e) {
                    LOGGER.warn("Message {} wasn't sent properly!", i, e.getCause());
                }
            } else {
                // Increment number of sent messages for non blocking producer
                numSent.incrementAndGet();
            }
            if (transactionalProducer && (i + 1) % msgPerTx == 0) {
                LOGGER.info("Committing the transaction. Messages sent: {}", i);
                producer.commitTransaction();
            }

            Thread.sleep(config.getDelay());
        }

        LOGGER.info("{} messages sent ...", numSent.get());
        producer.close();

        if (exit) {
            System.exit(numSent.get() == config.getMessageCount() ? 0 : 1);
        }
    }
}
