/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.http.producer;

import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.skodjob.datagenerator.DataGenerator;
import io.skodjob.datagenerator.enums.ETemplateType;
import io.strimzi.testclients.common.ClientsInterface;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.http.HttpProducerConfiguration;
import io.strimzi.testclients.common.records.producer.http.OffsetRecordSent;
import io.strimzi.testclients.common.records.producer.http.OffsetRecordSentUtils;
import io.strimzi.testclients.common.records.producer.http.ProducerRecord;
import io.strimzi.testclients.http.HttpBridgeClientUtil;
import io.strimzi.testclients.tracing.HttpContext;
import io.strimzi.testclients.tracing.HttpHandle;
import io.strimzi.testclients.tracing.TracingHandle;
import io.strimzi.testclients.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpProducerClient implements ClientsInterface {
    private static final Logger LOGGER = LogManager.getLogger(HttpProducerClient.class);
    private final HttpProducerConfiguration configuration;
    private int messageIndex;
    private int messageSuccessfullySent;
    private HttpClient client;
    private TracingHandle tracingHandle;
    private HttpHandle httpHandle;
    private final ScheduledExecutorService scheduledExecutor;
    private final CountDownLatch countDownLatch;
    private DataGenerator dataGenerator;

    public HttpProducerClient(Map<String, String> configuration) {
        this.configuration = new HttpProducerConfiguration(configuration);
        this.messageIndex = 0;
        this.client = createHttpClient();
        this.tracingHandle = TracingUtil.getTracing();
        this.httpHandle = tracingHandle.createHttpHandle("send-messages");
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "http-producer"));
        this.countDownLatch = new CountDownLatch(1);

        if (this.configuration.getMessageTemplate() != null) {
            dataGenerator = new DataGenerator(ETemplateType.getFromString(this.configuration.getMessageTemplate()));
        }
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration.toString());

        if (configuration.getDelay() == 0) {
            sendMessages();
        } else {
            scheduledExecutor.scheduleAtFixedRate(this::checkAndSendMessages, ConfigurationConstants.DEFAULT_DELAY_MS, configuration.getDelay(), TimeUnit.MILLISECONDS);
            awaitCompletion();
        }

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
        if (messageSuccessfullySent == configuration.getMessageCount()) {
            LOGGER.info("All messages successfully sent");
        } else {
            LOGGER.error("Unable to correctly send all messages");
            throw new RuntimeException("Failed to send all messages");
        }
    }

    private HttpClient createHttpClient() {
        return HttpBridgeClientUtil.getHttpClient(configuration.getSslTruststoreCertificate());
    }

    public void checkAndSendMessages() {
        if (messageIndex == configuration.getMessageCount()) {
            LOGGER.info("Shutting down the executor");
            scheduledExecutor.shutdown();
            countDownLatch.countDown();
        } else {
            try {
                this.sendMessages();
            } catch (Exception e) {
                LOGGER.error("Caught exception: {}", e.getMessage());
                e.printStackTrace();
                scheduledExecutor.shutdown();
                countDownLatch.countDown();
            }
        }
    }

    public ProducerRecord generateMessage(int numOfMessage) {
        Object message;
        if (this.configuration.getMessageTemplate() != null) {
            message = dataGenerator.generateData();
        } else {
            message = configuration.getMessage() + "-" + numOfMessage;
        }

        if (Objects.equals(this.configuration.getMessageType(), ConfigurationConstants.DEFAULT_MESSAGE_TYPE)) {
            message = "\"" + message + "\"";
        }

        String record = "{\"records\":[{\"key\":\"key-" + numOfMessage + "\",\"value\":" + message + "}]}";

        HttpContext context = HttpContext.post(
            configuration.getUri(),
            "application/vnd.kafka." + this.configuration.getMessageType() + ".v2+json",
            record);

        messageIndex++;

        return new ProducerRecord(record, context);
    }

    public ProducerRecord generateMessages() {
        String record = "{\"records\":[";

        for (int i = 0; i < configuration.getMessageCount(); i++) {
            Object message;
            if (this.configuration.getMessageTemplate() != null) {
                message = dataGenerator.generateData();
            } else {
                message = configuration.getMessage() + "-" + i;
            }

            if (Objects.equals(this.configuration.getMessageType(), ConfigurationConstants.DEFAULT_MESSAGE_TYPE)) {
                message = "\"" + message + "\"";
            }

            record += "{\"key\":\"key-" + i + "\",\"value\":" + message + "}";
            if (i != configuration.getMessageCount() - 1) {
                record += ",";
            }
        }

        record += "]}";

        HttpContext context = HttpContext.post(
            configuration.getUri(),
            "application/vnd.kafka." + this.configuration.getMessageType() + ".v2+json",
            record);

        messageIndex = configuration.getMessageCount() - 1;
        return new ProducerRecord(record, context);
    }

    public void sendMessages() {
        ProducerRecord producerRecord = configuration.getDelay() == 0 ? generateMessages() : generateMessage(messageIndex);

        try {
            LOGGER.info("Sending message(s): {}", producerRecord.message());

            HttpResponse httpResponse = httpHandle.finish(client.send(httpHandle.build(producerRecord.context()), HttpResponse.BodyHandlers.ofString()));

            if (httpResponse.statusCode() != HttpResponseStatus.OK.code()) {
                LOGGER.error("Error while sending message {} : {}", producerRecord.message(), httpResponse.body());
                throw new RuntimeException("Failed to send message due to: " + httpResponse.body());
            } else if (httpResponse.body().equals("[]") && httpResponse.statusCode() == HttpResponseStatus.OK.code()) {
                LOGGER.info("Array with messages is empty, no messages were received!");
            }

            OffsetRecordSent[] offsetRecordSent = OffsetRecordSentUtils.parseOffsetRecordsSent(httpResponse.body().toString());
            OffsetRecordSentUtils.logOffsetRecordsSent(offsetRecordSent);
            messageSuccessfullySent += offsetRecordSent.length;
        } catch (Exception e) {
            LOGGER.error("Caught exception during message send");
            e.printStackTrace();
            throw new RuntimeException("Failed to send message due to: " + e.getMessage());
        }
    }
}
