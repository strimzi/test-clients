/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.producer.http;

import io.strimzi.common.records.producer.http.ProducerRecord;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.http.producer.HttpProducerClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpProducerClientTest {

    private HttpProducerClient producerClient;
    private Map<String, String> configuration = new HashMap<>();

    @Test
    void testGenerateMessageDefault() {
        producerClient = new HttpProducerClient(configuration);
        int numberOfMessage = 6;

        String desiredJsonMessage = "{\"records\":[{\"key\":\"key-" + numberOfMessage + "\",\"value\":\"" + ConfigurationConstants.DEFAULT_MESSAGE + "-" + numberOfMessage + "\"}]}";

        ProducerRecord result = producerClient.generateMessage(numberOfMessage);

        assertThat(result.message(), is(desiredJsonMessage));
    }

    @Test
    void testGenerateMessageText() {
        configuration.put("MESSAGE_TYPE", "text");
        producerClient = new HttpProducerClient(configuration);
        int numberOfMessage = 6;

        String desiredJsonMessage = "{\"records\":[{\"key\":\"key-" + numberOfMessage + "\",\"value\":\"" + ConfigurationConstants.DEFAULT_MESSAGE + "-" + numberOfMessage + "\"}]}";

        ProducerRecord result = producerClient.generateMessage(numberOfMessage);

        assertThat(result.message(), is(desiredJsonMessage));
    }

    @Test
    void testGenerateMessageJson() {
        configuration.put("MESSAGE_TYPE", "json");
        producerClient = new HttpProducerClient(configuration);
        int numberOfMessage = 6;

        String desiredJsonMessage = "{\"records\":[{\"key\":\"key-" + numberOfMessage + "\",\"value\":" + ConfigurationConstants.DEFAULT_MESSAGE + "-" + numberOfMessage + "}]}";

        ProducerRecord result = producerClient.generateMessage(numberOfMessage);

        assertThat(result.message(), is(desiredJsonMessage));
    }

    @BeforeAll
    void setup() {
        configuration.put("HOSTNAME", "localhost");
        configuration.put("PORT", "8080");
        configuration.put("TOPIC", "my-topic");
    }
}
