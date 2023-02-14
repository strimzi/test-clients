/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.common.ClientsInterface;
import io.strimzi.common.configuration.http.HttpProducerConfiguration;
import io.strimzi.test.tracing.HttpContext;
import io.strimzi.test.tracing.HttpHandle;
import io.strimzi.test.tracing.TracingHandle;
import io.strimzi.test.tracing.TracingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
    private CountDownLatch latch = new CountDownLatch(1);

    public HttpProducerClient() {
        this.configuration = new HttpProducerConfiguration(System.getenv());
        this.messageIndex = 0;
        this.client = HttpClient.newHttpClient();
        this.tracingHandle = TracingUtil.getTracing();
        this.httpHandle = tracingHandle.createHttpHandle("send-messages");
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "http-producer"));
    }

    @Override
    public void run() throws Exception {
        LOGGER.info("Starting {} with configuration: \n{}", this.getClass().getName(), configuration.toString());
        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(this::sendMessage, 0,  configuration.getDelay(), TimeUnit.MILLISECONDS);

        scheduledExecutor.submit(() -> checkForCompletion(future));

        latch.await();
        scheduledExecutor.shutdown();

        if (messageIndex == configuration.getMessageCount() - 1) {
            if (messageSuccessfullySent == configuration.getMessageCount() - 1) {
                LOGGER.info("All messages successfully sent");
            } else {
                LOGGER.error("Unable to correctly send all messages");
                throw new RuntimeException("Failed to send all messages");
            }
        }
    }

    @Override
    public void checkForCompletion(ScheduledFuture<?> future) {
        if (messageIndex == configuration.getMessageCount() - 1) {
            future.cancel(false);
            latch.countDown();
        }
    }

    private ProducerRecord generateMessage(int numOfMessage) {
        String record = "{\"records\":[{\"key\":\"key-" + numOfMessage + "\",\"value\":\"" + configuration.getMessage() + "-" + numOfMessage + "\"}]}";

        HttpContext context = HttpContext.post(
            configuration.getUri(),
            "application/vnd.kafka.json.v2+json",
            record);

        messageIndex++;

        return new ProducerRecord(record, context);
    }

    public CompletableFuture<Void> sendMessage() {
        ProducerRecord producerRecord = generateMessage(messageIndex);
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            HttpResponse httpResponse = httpHandle.finish(client.send(httpHandle.build(producerRecord.context()), HttpResponse.BodyHandlers.ofString()));

            if (httpResponse.statusCode() != HttpResponseStatus.OK.code()) {
                LOGGER.error("Error while sending message {} : {}", producerRecord.message(), httpResponse.body());
                future.completeExceptionally(new RuntimeException("Failed to send message due to: " + httpResponse.body()));
            } else if (httpResponse.body().equals("[]") && httpResponse.statusCode() == HttpResponseStatus.OK.code()) {
                LOGGER.info("Array with messages is empty, no messages were received!");
            }

            OffsetRecordSent[] offsetRecordSent = parseOffsetRecordsSent(httpResponse.body().toString());
            logOffsetRecordsSent(offsetRecordSent);
            messageSuccessfullySent++;
            future.complete(null);
        } catch (Exception e) {
            LOGGER.error("Caught exception during message send");
            e.printStackTrace();
            future.completeExceptionally(new RuntimeException("Failed to send message due to: " + e.getMessage()));
        }

        return future;
    }

    private void logOffsetRecordsSent(OffsetRecordSent[] offsetRecordsSent) {
        for (OffsetRecordSent offsetRecordSent : offsetRecordsSent) {
            LOGGER.info(offsetRecordSent.toString());
        }
    }

    public OffsetRecordSent[] parseOffsetRecordsSent(String response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode json = objectMapper.readTree(response);
        String offsets = json.get("offset").toString();

        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        return objectMapper.readValue(offsets, OffsetRecordSent[].class);
    }
}
