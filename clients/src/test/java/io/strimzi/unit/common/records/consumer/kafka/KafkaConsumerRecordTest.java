/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.unit.common.records.consumer.kafka;

import io.strimzi.common.records.consumer.kafka.KafkaConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public class KafkaConsumerRecordTest {

    @Test
    void testParseKafkaConsumerRecordsToJson() {
        String topic = "random-topic";
        String key = "key-0";
        String value = "Hello world-0";
        int partition = 0;
        long offset = 0;
        long timestamp = -1;
        TimestampType timestampType = TimestampType.NO_TIMESTAMP_TYPE;
        int serializedKeySize = -1;
        int serializedValueSize = -1;
        Optional<Integer> leaderEpoch = Optional.empty();
        Headers headers = new RecordHeaders();
        headers.add("header-key", "header-value".getBytes());
        headers.add("header-key-2", "header-value-2".getBytes());

        ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(topic, partition, offset, timestamp, timestampType, serializedKeySize, serializedValueSize,
                key, value, headers, leaderEpoch);

        String jsonLog = KafkaConsumerRecord.parseKafkaConsumerRecord(consumerRecord).logMessage("json");
        String plainLog = KafkaConsumerRecord.parseKafkaConsumerRecord(consumerRecord).logMessage("plain");

        String expectedResult = "{\"timestamp\":" + timestamp + ",\"timestampType\":\"" + timestampType.name + "\",\"topic\":\"" + topic + "\"," +
                "\"partition\":" + partition + ",\"offset\":" + offset + ",\"key\":\"" + key + "\"," +
                "\"payload\":\"" + value + "\",\"headers\":[{\"header-key\":\"header-value\"},{\"header-key-2\":\"header-value-2\"}]}";

        String expectedPlainLog = """

                \ttopic: random-topic
                \tpartition: 0
                \toffset: 0
                \tkey: key-0\

                \tvalue: Hello world-0
                \theaders:\s
                \t\tkey: header-key, value: header-value\

                \t\tkey: header-key-2, value: header-value-2""";

        assertAll(
                () -> assertThat(jsonLog, is(expectedResult)),
                () -> assertThat(plainLog, is(expectedPlainLog))
        );
    }
}
