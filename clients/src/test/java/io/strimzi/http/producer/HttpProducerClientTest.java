/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.producer;

import io.strimzi.common.records.producer.http.ProducerRecord;
import io.strimzi.configuration.ConfigurationConstants;
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

    @Test
    void testGenerateMessage() {
        int numberOfMessage = 6;

        String desiredJsonMessage = "{\"records\":[{\"key\":\"key-" + numberOfMessage + "\",\"value\":\"" + ConfigurationConstants.DEFAULT_MESSAGE + "-" + numberOfMessage + "\"}]}";

        ProducerRecord result = producerClient.generateMessage(numberOfMessage);

        assertThat(result.message(), is(desiredJsonMessage));
    }

    @BeforeAll
    void setup() {
        Map<String, String> configuration = new HashMap<>();

        configuration.put("HOSTNAME", "localhost");
        configuration.put("PORT", "8080");
        configuration.put("TOPIC", "my-topic");

        producerClient = new HttpProducerClient(configuration);
    }
}
