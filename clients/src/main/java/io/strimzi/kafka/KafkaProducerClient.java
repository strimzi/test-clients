/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka;

import io.strimzi.common.ClientsInterface;
import io.strimzi.common.configuration.Constants;
import io.strimzi.common.configuration.kafka.KafkaProducerConfiguration;
import io.strimzi.common.properties.KafkaProperties;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaProducerClient implements ClientsInterface {

    private static final Logger LOGGER = LogManager.getLogger(KafkaProducerClient.class);
    private final KafkaProducerConfiguration configuration;
    private final Properties properties;
    private final KafkaProducer producer;
    private int messageIndex;
    private int messageSuccessfullySent;
    private final ScheduledExecutorService scheduledExecutor;

    public KafkaProducerClient(Map<String, String> configuration) {
        this.configuration = new KafkaProducerConfiguration(configuration);
        this.properties =  KafkaProperties.producerProperties(this.configuration);
        TracingUtil.initialize().addTracingPropsToProducerConfig(properties);

        this.producer = new KafkaProducer<>(this.properties);
        this.messageIndex = 0;
        this.messageSuccessfullySent = 0;
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "kafka-producer"));
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration.toString());

        if (configuration.isTransactionalProducer()) {
            LOGGER.info("Using transactional producer. Initializing the transactions.");
            producer.initTransactions();
        }

        // in case we want to send all messages immediately, we have to schedule just one task
        if (configuration.getDelayMs() == 0) {
            scheduledExecutor.schedule(this::sendMessages, Constants.DEFAULT_DELAY_MS, TimeUnit.MILLISECONDS);
            scheduledExecutor.shutdown();
        } else {
            scheduledExecutor.scheduleAtFixedRate(this::checkAndSendMessages, Constants.DEFAULT_DELAY_MS, configuration.getDelayMs(), TimeUnit.MILLISECONDS);
        }

        awaitCompletion();
    }

    @Override
    public void awaitCompletion() {
        try {
            long timeoutForOperations = configuration.getMessageCount() * configuration.getDelayMs() + Constants.DEFAULT_TASK_COMPLETION_TIMEOUT;
            scheduledExecutor.awaitTermination(timeoutForOperations, TimeUnit.MILLISECONDS);

            if (messageSuccessfullySent == configuration.getMessageCount()) {
                LOGGER.info("All messages successfully sent");
            } else {
                LOGGER.error("Unable to correctly send all messages");
                throw new RuntimeException("Failed to send all messages");
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

    public void checkAndSendMessages() {
        if (messageIndex == configuration.getMessageCount()) {
            scheduledExecutor.shutdown();
        } else {
            this.sendMessages();
        }
    }

    public ProducerRecord generateMessage(int numOfMessage) {
        return new ProducerRecord(configuration.getTopicName(), null, null, null,
            "\"" + configuration.getMessage() + " - " + numOfMessage + "\"", configuration.getHeaders());
    }

    public List<ProducerRecord> generateMessages() {
        List<ProducerRecord> records = new ArrayList<>();
        for (int i = 0; i < configuration.getMessageCount(); i++) {
            records.add(generateMessage(i));
        }

        return records;
    }

    public void sendMessages() {
        List<ProducerRecord> records = configuration.getDelayMs() == 0 ? generateMessages() : Collections.singletonList(generateMessage(messageIndex));
        messageIndex += records.size();

        int currentMsgIndex = configuration.getDelayMs() == 0 ? 0 : messageIndex;

        for (ProducerRecord record : records) {

            if (configuration.isTransactionalProducer() && currentMsgIndex % configuration.getMessagesPerTransaction() == 0) {
                LOGGER.info("Beginning new transaction. Messages sent: {}", currentMsgIndex);
                producer.beginTransaction();
            }
            LOGGER.info("Sending message: {}", record.toString());

            try {
                producer.send(record).get();
                messageSuccessfullySent++;
            } catch (Exception e) {
                LOGGER.error("Failed to send messages: {} due to: \n{}", record.toString(), e.getMessage());
            }

            if (configuration.isTransactionalProducer() && (currentMsgIndex + 1) % configuration.getMessagesPerTransaction() == 0) {
                LOGGER.info("Committing the transaction. Messages sent: {}", currentMsgIndex);
                producer.commitTransaction();
            }
        }
    }
}
