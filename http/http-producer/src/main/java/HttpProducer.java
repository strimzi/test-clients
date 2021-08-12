/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.http.HttpHeaderNames;

public class HttpProducer {

    private static final Logger LOGGER = LogManager.getLogger(HttpProducer.class);

    private final HttpProducerConfiguration producerConfig;

    public HttpProducer(HttpProducerConfiguration producerConfig) {
        this.producerConfig = producerConfig;
    }

    private List<ProducerRecord> generateMessages() throws URISyntaxException {
        List<ProducerRecord> requests = new ArrayList<>();

        for (int i = 0; i < this.producerConfig.getMessageCount(); i++) {
            String record = "{\"records\":[{\"key\":\"key-" + i + "\",\"value\":\"" + this.producerConfig.getMessage() + "-" + i + "\"}]}";

            requests.add(new ProducerRecord(record, HttpRequest.newBuilder()
                .uri(new URI(this.producerConfig.getUri()))
                .setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.json.v2+json")
                .POST(HttpRequest.BodyPublishers.ofString(record))
                .build()
            ));
        }

        return requests;
    }

    public boolean sendMessages() throws URISyntaxException, InterruptedException, ExecutionException {
        HttpClient client = HttpClient.newHttpClient();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        List<ProducerRecord> records = generateMessages();

        boolean[] sendSuccessful = {true};
        UncheckedObjectMapper uncheckedObjectMapper = new UncheckedObjectMapper();

        for (ProducerRecord record : records) {
            executorService.schedule(() -> {
                try {
                    LOGGER.info("Sending message: {}", record.getMessage());
                    List<OffsetRecordSent> offsetRecords = Arrays.asList(client.sendAsync(record.getRequest(), HttpResponse.BodyHandlers.ofString())
                        .thenApply(result -> {
                            if (result.statusCode() != HttpResponseStatus.OK.code()) {
                                LOGGER.error("Error while sending message {} : {}", record.getMessage(), result.body());
                                sendSuccessful[0] = false;
                            }
                            return new JsonObject(result.body()).getJsonArray("offsets").toString();
                        })
                        .thenApply(uncheckedObjectMapper::readValue)
                        .get());

                    offsetRecords.forEach(offsetRecord -> LOGGER.info("{}", offsetRecord.toString()));
                } catch (Exception e) {
                    LOGGER.error("Exception while executing send request. Message: {} and cause: {}", e.getMessage(), e.getCause());
                    sendSuccessful[0] = false;
                }
            }, this.producerConfig.getDelay(), TimeUnit.MILLISECONDS).get();
        }

        executorService.shutdown();
        boolean status = sendSuccessful[0] &&
            executorService.awaitTermination(producerConfig.getDelay() * producerConfig.getMessageCount() + 60_000, TimeUnit.MILLISECONDS);

        if (!status) {
            LOGGER.error("Unable to correctly send all messages!");
        } else {
            LOGGER.info("All messages successfully sent!");
        }
        return status;
    }
}

class UncheckedObjectMapper extends ObjectMapper {
    OffsetRecordSent[] readValue(String content) {
        try {
            return this.readValue(content, new TypeReference<>() {
            });
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }
}