/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.consumer;

import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.common.ClientsInterface;
import io.strimzi.common.configuration.Constants;
import io.strimzi.common.configuration.http.HttpConsumerConfiguration;
import io.strimzi.common.records.http.consumer.ConsumerRecord;
import io.strimzi.common.records.http.consumer.ConsumerRecordUtils;
import io.strimzi.test.tracing.HttpContext;
import io.strimzi.test.tracing.HttpHandle;
import io.strimzi.test.tracing.TracingHandle;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpConsumerClient implements ClientsInterface {

    private static final Logger LOGGER = LogManager.getLogger(HttpConsumerClient.class);
    private final HttpConsumerConfiguration configuration;
    private int consumedMessages;
    private HttpClient client;
    private TracingHandle tracingHandle;
    private HttpHandle httpHandle;
    private final ScheduledExecutorService scheduledExecutor;

    public HttpConsumerClient(Map<String, String> configuration) {
        this.configuration = new HttpConsumerConfiguration(configuration);
        this.consumedMessages = 0;
        this.client = HttpClient.newHttpClient();
        this.tracingHandle = TracingUtil.getTracing();
        this.httpHandle = tracingHandle.createHttpHandle("receive-messages");
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "http-consumer"));
    }

    @Override
    public void run() {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration.toString());

        createConsumer();
        subscribeToTopic();
        scheduledExecutor.scheduleWithFixedDelay(this::checkAndReceiveMessages, 0, configuration.getPollInterval(), TimeUnit.MILLISECONDS);

        awaitCompletion();
    }

    @Override
    public void awaitCompletion() {
        try {
            long timeoutForOperations = configuration.getMessageCount() * configuration.getPollInterval() + Constants.DEFAULT_TASK_COMPLETION_TIMEOUT;
            scheduledExecutor.awaitTermination(timeoutForOperations, TimeUnit.MILLISECONDS);

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
        } else {
            this.consumeMessages();
        }
    }

    public void createConsumer() {
        String message = "{\"name\":\"" + configuration.getClientId() + "\",\"format\":\"json\",\"auto.offset.reset\":\"earliest\"}";

        LOGGER.info("Creating new consumer:\n{}", message);

        HttpContext context = HttpContext.post(configuration.getConsumerCreationURI(), Constants.HTTP_JSON_CONTENT_TYPE, message);

        HttpHandle<String> httpHandle = TracingUtil.getTracing().createHttpHandle("create-consumer");
        HttpRequest creationRequest = httpHandle.build(context);

        try {
            HttpResponse<String> response = client.send(creationRequest, HttpResponse.BodyHandlers.ofString());
            httpHandle.finish(response);

            if (response.statusCode() == HttpResponseStatus.OK.code()) {
                LOGGER.info("Consumer successfully created. Response:\n{}", response.body());
            } else {
                throw new RuntimeException(String.format("Failed to create consumer. Status code: %s, response: %s", response.statusCode(), response.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to create consumer: %s due to: %s", configuration.getClientId(), e.getMessage()));
        }
    }

    public void subscribeToTopic() {
        String subscriptionMessage = "{\"topics\":[\"" + configuration.getTopic() + "\"]}";

        LOGGER.info("Subscribing consumer: {} to topic: {} with message: {}", configuration.getClientId(), configuration.getTopic(), subscriptionMessage);

        HttpContext context = HttpContext.post(configuration.getSubscriptionURI(), Constants.HTTP_JSON_CONTENT_TYPE, subscriptionMessage);

        HttpHandle<String> httpHandle = TracingUtil.getTracing().createHttpHandle("subscribe-topic");
        HttpRequest subscriptionRequest = httpHandle.build(context);

        try {
            HttpResponse<String> response = client.send(subscriptionRequest, HttpResponse.BodyHandlers.ofString());
            httpHandle.finish(response);

            if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                LOGGER.info("Successfully subscribed to topic {}. Response:\n{}", configuration.getTopic(), response.body());
            } else {
                throw new RuntimeException(String.format("Failed to subscribe consumer: %s to topic: %s. Response: %s", configuration.getClientId(), configuration.getTopic(), response.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to subscribe consumer: %s to topic: %s due to: %s", configuration.getClientId(), configuration.getTopic(), e.getMessage()));
        }
    }

    public void consumeMessages() {
        HttpContext context = HttpContext.get(configuration.getConsumeMessagesURI(), Constants.HTTP_JSON_CONTENT_TYPE);
        try {
            HttpResponse httpResponse = httpHandle.finish(client.send(httpHandle.build(context), HttpResponse.BodyHandlers.ofString()));

            if (httpResponse.statusCode() != HttpResponseStatus.OK.code()) {
                throw new RuntimeException("Failed to receive messages due to: " + httpResponse.body());
            } else if (httpResponse.body().equals("[]") && httpResponse.statusCode() == HttpResponseStatus.OK.code()) {
                LOGGER.info("Array with messages is empty, no messages were received");
            } else {
                ConsumerRecord[] records = ConsumerRecordUtils.parseConsumerRecordsFromJson(httpResponse.body().toString());
                ConsumerRecordUtils.logConsumerRecords(records);

                consumedMessages += records.length;
            }

        } catch (Exception e) {
            LOGGER.error("Failed to consume message due to: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
