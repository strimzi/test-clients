package io.strimzi.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;

import java.util.List;
import java.util.Map;

/**
 * The type Kafka consumer record.
 */
public record KafkaConsumerRecord(long timestamp, TimestampType timestampType, String topic, int partition, long offset,
                                  String key, String payload, List<Map<String, String>> headers) {
    /**
     * Gets topic.
     *
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets partition.
     *
     * @return the partition
     */
    public int getPartition() {
        return partition;
    }

    /**
     * Gets offset.
     *
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets timestamp type.
     *
     * @return the timestamp type
     */
    public String getTimestampType() {
        return timestampType.name;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public List<Map<String, String>> getHeaders() {
        return headers;
    }

    /**
     * Parse kafka consumer record.
     *
     * @param consumerRecord the consumer record
     * @return the kafka consumer record
     */
    public static KafkaConsumerRecord parseKafkaConsumerRecord(final ConsumerRecord<String,String> consumerRecord) {
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
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
