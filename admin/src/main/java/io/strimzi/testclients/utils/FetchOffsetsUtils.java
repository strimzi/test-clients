/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;

public class FetchOffsetsUtils {

    /**
     * Generates a string representation of a map of TopicPartition and ListOffsetsResultInfo objects by delegating
     * to either {@link #getPlain(Map)} or {@link #getJson(Map)}.
     *
     * @param outputFormat The output format to be used for generating the string representation.
     * @param kafkaTopicOffsets A map of TopicPartition and ListOffsetsResultInfo objects to be represented as a string.
     * @return A string representation of the provided map of TopicPartition and ListOffsetsResultInfo objects, formatted according to the specified output format.
     * @throws JsonProcessingException If an error occurs during JSON serialization when the output format is JSON.
     */
    public static String getOutput(OutputFormat outputFormat, Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> kafkaTopicOffsets) throws JsonProcessingException {
        return switch (outputFormat) {
            case PLAIN -> getPlain(kafkaTopicOffsets);
            case JSON -> getJson(kafkaTopicOffsets);
        };
    }

    /**
     * Generates a plain text string representation of a map of TopicPartition and ListOffsetsResultInfo objects.
     *
     * @param kafkaTopicOffsets A map of TopicPartition and ListOffsetsResultInfo objects to be represented as a string.
     * @return A plain text string representation of topics.
     */
    private static String getPlain(Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> kafkaTopicOffsets) {
        StringBuilder s = new StringBuilder();

        for (Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> entry : kafkaTopicOffsets.entrySet()) {
            final String singleRecord = String.format("Partition: %s, Offset: %s\n", entry.getKey().partition(), entry.getValue().offset());
            s.append(singleRecord);
        }

        return s.toString();
    }

    /**
     * Generates a JSON string representation of a map of TopicPartition and ListOffsetsResultInfo objects using Jackson's ObjectMapper.
     *
     * @param kafkaTopicOffsets A map of TopicPartition and ListOffsetsResultInfo objects.
     * @return A JSON string representation of topics.
     * @throws JsonProcessingException If an error occurs during serialization to JSON.
     */
    private static String getJson(Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> kafkaTopicOffsets) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> serializableMap = new HashMap<>();
        for (Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> entry : kafkaTopicOffsets.entrySet()) {
            Map<String, Object> offsetInfo = new HashMap<>();
            offsetInfo.put("offset", entry.getValue().offset());
            serializableMap.put(String.valueOf(entry.getKey().partition()), offsetInfo);
        }

        return mapper.writeValueAsString(serializableMap);
    }
}
