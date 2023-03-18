/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka;

import io.strimzi.common.ClientsInterface;
import io.strimzi.common.configuration.Constants;
import io.strimzi.common.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.common.properties.KafkaProperties;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaConsumerClient implements ClientsInterface {
    private static final Logger LOGGER = LogManager.getLogger(KafkaConsumerClient.class);
    private final KafkaConsumerConfiguration configuration;
    private final Properties properties;
    private final KafkaConsumer consumer;
    private int consumedMessages;
    private final ScheduledExecutorService scheduledExecutor;
    private final CountDownLatch countDownLatch;

    public KafkaConsumerClient(Map<String, String> configuration) {
        this.configuration = new KafkaConsumerConfiguration(configuration);
        this.properties = KafkaProperties.consumerProperties(this.configuration);
        TracingUtil.getTracing().addTracingPropsToConsumerConfig(properties);

        this.consumer = new KafkaConsumer(this.properties);
        this.consumedMessages = 0;
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "kafka-consumer"));
        this.countDownLatch  = new CountDownLatch(1);
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration.toString());

        consumer.subscribe(Collections.singletonList(configuration.getTopicName()));

        long delayMs = configuration.getDelayMs() == 0 ? Constants.DEFAULT_POLL_INTERVAL : configuration.getDelayMs();
        scheduledExecutor.scheduleWithFixedDelay(this::checkAndReceiveMessages, 0, delayMs, TimeUnit.MILLISECONDS);

        awaitCompletion();
    }

    @Override
    public void awaitCompletion() {
        try {
            countDownLatch.await();
            scheduledExecutor.awaitTermination(Constants.DEFAULT_TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);

            if (consumedMessages >= configuration.getMessageCount()) {
                LOGGER.info("All messages successfully received");
            } else {
                LOGGER.error("Unable to correctly receive all messages");
                throw new RuntimeException("Failed to receive all messages");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to wait for task completion due to: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (!scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdownNow();
            }
        }
    }

    private void checkAndReceiveMessages() {
        if (consumedMessages >= configuration.getMessageCount()) {
            scheduledExecutor.shutdown();
            countDownLatch.countDown();
        } else {
            try {
                this.consumeMessages();
            } catch (Exception e) {
                LOGGER.error("Caught exception: {}", e.getMessage());
                e.printStackTrace();
                scheduledExecutor.shutdown();
                countDownLatch.countDown();
            }
        }
    }

    public void consumeMessages() {
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
            consumedMessages++;
        }

        consumer.commitSync();
    }
}
