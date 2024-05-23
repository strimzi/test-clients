/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.records.consumer.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.strimzi.common.records.http.consumer.ConsumerRecord;
import io.strimzi.common.records.http.consumer.ConsumerRecordUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

public class ConsumerRecordUtilsTest {

    @Test
    void testParseConsumerRecordsFromJson() throws JsonProcessingException {
        String response = "[{\"topic\":\"random-topic\",\"key\":\"key-0\",\"value\":\"Hello world-0\",\"partition\":0,\"offset\":0}]";

        ConsumerRecord expectedResult = new ConsumerRecord();
        expectedResult.setPartition(0);
        expectedResult.setOffset(0);
        expectedResult.setTopic("random-topic");
        expectedResult.setKey("key-0");
        expectedResult.setValue("Hello world-0");

        ConsumerRecord[] result = ConsumerRecordUtils.parseConsumerRecordsFromJson(response);

        assertThat(result.length, is(1));
        assertThat(result[0], is(expectedResult));
    }

    @Test
    void testParseConsumerRecordsWithWrongValue() {
        String response = "Completely random response";

        assertThrows(JsonProcessingException.class, () -> ConsumerRecordUtils.parseConsumerRecordsFromJson(response));
    }
}
