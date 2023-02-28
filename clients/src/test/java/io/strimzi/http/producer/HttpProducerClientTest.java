/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.strimzi.common.configuration.Constants;
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

        String desiredJsonMessage = "{\"records\":[{\"key\":\"key-" + numberOfMessage + "\",\"value\":\"" + Constants.DEFAULT_MESSAGE + "-" + numberOfMessage + "\"}]}";

        ProducerRecord result = producerClient.generateMessage(numberOfMessage);

        assertThat(result.message(), is(desiredJsonMessage));
    }

    @Test
    void testParseOffsetRecordsSent() throws JsonProcessingException {
        String responseExample = "{\"offsets\":[{\"partition\":0,\"offset\":16}]}";

        OffsetRecordSent expectedResult = new OffsetRecordSent();
        expectedResult.setOffset(16);
        expectedResult.setPartition(0);

        OffsetRecordSent[] result = producerClient.parseOffsetRecordsSent(responseExample);

        assertThat(result.length, is(1));
        assertThat(result[0], is(expectedResult));
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
