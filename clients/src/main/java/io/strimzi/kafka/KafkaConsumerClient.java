/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka;

import io.strimzi.common.ClientsInterface;
import io.strimzi.common.properties.KafkaProperties;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.configuration.kafka.KafkaConsumerConfiguration;
import io.strimzi.common.records.consumer.kafka.KafkaConsumerRecord;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
    private final KafkaConsumer consumer;
    private int consumedMessages;
    private final ScheduledExecutorService scheduledExecutor;
    private final CountDownLatch countDownLatch;

    public KafkaConsumerClient(Map<String, String> configuration) {
        this.configuration = new KafkaConsumerConfiguration(configuration);
        Properties properties = KafkaProperties.consumerProperties(this.configuration);
        TracingUtil.getTracing().addTracingPropsToConsumerConfig(properties);

        this.consumer = new KafkaConsumer(properties);
        this.consumedMessages = 0;
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "kafka-consumer"));
        this.countDownLatch  = new CountDownLatch(1);
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration);

        consumer.subscribe(Collections.singletonList(configuration.getTopicName()));

        long delayMs = configuration.getDelayMs() == 0 ? ConfigurationConstants.DEFAULT_POLL_INTERVAL : configuration.getDelayMs();
        scheduledExecutor.scheduleWithFixedDelay(this::checkAndReceiveMessages, 0, delayMs, TimeUnit.MILLISECONDS);

        awaitCompletion();
        checkFinalState();
    }

    @Override
    public void awaitCompletion() {
        try {
            countDownLatch.await();
            scheduledExecutor.awaitTermination(ConfigurationConstants.DEFAULT_TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to wait for task completion due to: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (!scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdownNow();
            }
        }
    }

    @Override
    public void checkFinalState() {
        if (consumedMessages >= configuration.getMessageCount()) {
            LOGGER.info("All messages successfully received");
        } else {
            LOGGER.error("Unable to correctly receive all messages");
            throw new RuntimeException("Failed to receive all messages");
        }
    }

    private void checkAndReceiveMessages() {
        if (consumedMessages >= configuration.getMessageCount()) {
            LOGGER.info("Shutting down the executor");
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
        ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
        int recordProcessed = 0;

        for (ConsumerRecord<Object, Object> consumerRecord : records) {
            KafkaConsumerRecord kafkaConsumerRecord = KafkaConsumerRecord.parseKafkaConsumerRecord(consumerRecord);
            String log = kafkaConsumerRecord.logMessage(configuration.getOutputFormat());
            LOGGER.info("Received message: {}", log);
            recordProcessed++;
        }

        try {
            consumer.commitSync();
            consumedMessages += recordProcessed;
        } catch (Exception ex) {
            LOGGER.warn("Failed to commit the offset: {}", ex.getMessage());
        }
    }
}
