/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.unit.common.records.producer.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.strimzi.testclients.common.records.producer.http.OffsetRecordSent;
import io.strimzi.testclients.common.records.producer.http.OffsetRecordSentUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OffsetRecordSentUtilsTest {

    @Test
    void testParseOffsetRecordsSent() throws JsonProcessingException {
        String responseExample = "{\"offsets\":[{\"partition\":0,\"offset\":16}]}";

        OffsetRecordSent expectedResult = new OffsetRecordSent();
        expectedResult.setOffset(16);
        expectedResult.setPartition(0);

        OffsetRecordSent[] result = OffsetRecordSentUtils.parseOffsetRecordsSent(responseExample);

        assertThat(result.length, is(1));
        assertThat(result[0], is(expectedResult));
    }

    @Test
    void testParseConsumerRecordsWithWrongValue() {
        String response = "Completely random response";

        assertThrows(JsonProcessingException.class, () -> OffsetRecordSentUtils.parseOffsetRecordsSent(response));
    }
}
