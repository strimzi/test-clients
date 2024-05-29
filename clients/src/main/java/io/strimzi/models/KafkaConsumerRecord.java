/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Kafka consumer record.
 */
public record KafkaConsumerRecord(long timestamp, TimestampType timestampType, String topic, int partition, long offset,
                                  String key, String payload, List<Map<String, String>> headers) {
    /**
     * Gets timestamp type.
     *
     * @return the timestamp type
     */
    public String getTimestampType() {
        return timestampType.name;
    }

    /**
     * Parse kafka consumer record.
     *
     * @param consumerRecord the consumer record
     * @return the kafka consumer record
     */
    public static KafkaConsumerRecord parseKafkaConsumerRecord(final ConsumerRecord<String, String> consumerRecord) {
        List<Map<String, String>> headers = new ArrayList<>();
        consumerRecord.headers().forEach(h -> headers.add(Map.of(h.key(), new String(h.value(), StandardCharsets.UTF_8))));
        return new KafkaConsumerRecord(
                consumerRecord.timestamp(),
                consumerRecord.timestampType(),
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.key(),
                consumerRecord.value(),
                headers);
    }

    /**
     * To json string.
     *
     * @return the string
     */
    public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Log message.
     *
     * @param outputFormat the output format
     */
    public String logMessage(String outputFormat) {
        String log;
        if (outputFormat.equalsIgnoreCase("json")) {
            log = this.toJsonString();
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("\n\ttopic: ").append(this.topic());
            builder.append("\n\tpartition: ").append(this.partition());
            builder.append("\n\toffset: ").append(this.offset());
            builder.append("\n\tkey: ").append(this.key());
            builder.append("\n\tvalue: ").append(this.payload());
            if (this.headers() != null) {
                builder.append("\n\theaders: ");
                for (Map<String, String> header : this.headers()) {
                    for (Map.Entry<String, String> entry : header.entrySet()) {
                        builder.append("\n\t\tkey: ").append(entry.getKey()).append(", value: ").append(entry.getValue());
                    }
                }
            }
            log = builder.toString();
        }
        return log;
    }
}
