/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.test.tracing.HttpContext;
import io.strimzi.test.tracing.HttpHandle;
import io.strimzi.test.tracing.TracingHandle;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class HttpConsumer {

    private static final Logger LOGGER = LogManager.getLogger(HttpConsumer.class);

    private final HttpConsumerConfiguration consumerConfig;
    private final HttpClient client;

    public HttpConsumer(HttpConsumerConfiguration consumerConfig) {
        this.consumerConfig = consumerConfig;
        this.client = HttpClient.newHttpClient();
    }

    public void createConsumer() throws URISyntaxException, IOException, InterruptedException {
        String consumerCreationURI = "http://" + consumerConfig.getHostname() + ":" + consumerConfig.getPort() + "/consumers/" + consumerConfig.getGroupId();
        String message = "{\"name\":\"" + consumerConfig.getClientId() + "\",\"format\":\"json\",\"auto.offset.reset\":\"earliest\"}";

        LOGGER.info("Creating new consumer:\n{}", message);

        HttpContext context = HttpContext.post(consumerCreationURI, "application/vnd.kafka.v2+json", message);

        HttpHandle<String> httpHandle = TracingUtil.getTracing().createHttpHandle("create-consumer");
        HttpRequest creationRequest = httpHandle.build(context);

        HttpResponse<String> response = client.send(creationRequest, HttpResponse.BodyHandlers.ofString());
        httpHandle.finish(response);

        if (response.statusCode() == HttpResponseStatus.OK.code()) {
            LOGGER.info("Consumer successfully created. Response:\n{}", response.body());
        } else {
            throw new RuntimeException(String.format("Failed to create consumer. Status code: %s, response: %s", response.statusCode(), response.body()));
        }
    }

    public void subscribeToTopic() throws URISyntaxException, IOException, InterruptedException {
        String subscriptionURI = "http://" + consumerConfig.getHostname() + ":" + consumerConfig.getPort() + "/consumers/" + consumerConfig.getGroupId() + "/instances/" + consumerConfig.getClientId() + "/subscription";
        String subscriptionMessage = "{\"topics\":[\"" + consumerConfig.getTopic() + "\"]}";

        LOGGER.info("Subscribing consumer: {} to topic: {} with message: {}", consumerConfig.getClientId(), consumerConfig.getTopic(), subscriptionMessage);

        HttpContext context = HttpContext.post(subscriptionURI, "application/vnd.kafka.v2+json", subscriptionMessage);

        HttpHandle<String> httpHandle = TracingUtil.getTracing().createHttpHandle("subscribe-topic");
        HttpRequest subscriptionRequest = httpHandle.build(context);

        HttpResponse<String> response = client.send(subscriptionRequest, HttpResponse.BodyHandlers.ofString());
        httpHandle.finish(response);

        if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
            LOGGER.info("Successfully subscribed to topic {}. Response:\n{}", consumerConfig.getTopic(), response.body());
        } else {
            throw new RuntimeException(String.format("Failed to subscribe consumer: %s to topic: %s. Response: %s", consumerConfig.getClientId(), consumerConfig.getTopic(), response.body()));
        }
    }

    public boolean consumeMessages() throws URISyntaxException, InterruptedException, ExecutionException {
        String recordsURI = "http://" + consumerConfig.getHostname() + ":" + consumerConfig.getPort() + "/consumers/" + consumerConfig.getGroupId() + "/instances/" + consumerConfig.getClientId() + "/records?timeout=" + consumerConfig.getPollTimeout();

        TracingHandle tracing = TracingUtil.getTracing();
        HttpContext context = HttpContext.get(recordsURI, "application/vnd.kafka.json.v2+json");

        UncheckedObjectMapper uncheckedObjectMapper = new UncheckedObjectMapper();

        Executor delayedExecutor = CompletableFuture.delayedExecutor(consumerConfig.getPollInterval(), TimeUnit.MILLISECONDS);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Runnable request = new Runnable() {
            private int receivedMessages;

            @Override
            public void run() {
                HttpHandle<String> httpHandle = tracing.createHttpHandle("consume-messages");
                boolean result = false;
                try {
                    List<ConsumerRecord> records = Arrays.asList(client.sendAsync(httpHandle.build(context), HttpResponse.BodyHandlers.ofString())
                        .thenApply(httpHandle::finish)
                        .thenApply(HttpResponse::body)
                        .thenApply(uncheckedObjectMapper::readValue)
                        .get());

                    records.forEach(consumerRecord -> LOGGER.info("{}", consumerRecord.toString()));
                    receivedMessages += records.size();
                    if (receivedMessages >= consumerConfig.getMessageCount()) {
                        result = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception while executing GET request. Message: {} and cause: {}", e.getMessage(), e.getCause());
                    result = false;
                }
                if (result) {
                    future.complete(true);
                } else {
                    delayedExecutor.execute(this);
                }
            }
        };

        request.run();
        boolean status = future.get();

        if (!status) {
            LOGGER.error("Unable to correctly receive all messages!");
        } else {
            LOGGER.info("All messages successfully received!");
        }
        return status;
    }
}

class UncheckedObjectMapper extends ObjectMapper {
    ConsumerRecord[] readValue(String content) {
        try {
            return this.readValue(content, new TypeReference<>() {
            });
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }
}
