/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.strimzi.testclients.models.KafkaTopicDescription;

import java.util.List;

public class DescribeTopicsUtils {

    /**
     * Generates a string representation of a list of KafkaTopicDescription objects by delegating
     * to either {@link #getPlain(List)} or {@link #getJson(List)}.
     *
     * @param outputFormat The output format to be used for generating the string representation.
     * @param kafkaTopicDescriptionList A list of KafkaTopicDescription objects to be represented as a string.
     * @return A string representation of the provided list of KafkaTopicDescription objects, formatted according to the specified output format.
     * @throws JsonProcessingException If an error occurs during JSON serialization when the output format is JSON.
     */
    public static String getOutput(OutputFormat outputFormat, List<KafkaTopicDescription> kafkaTopicDescriptionList) throws JsonProcessingException {
        return switch (outputFormat) {
            case PLAIN -> getPlain(kafkaTopicDescriptionList);
            case JSON -> getJson(kafkaTopicDescriptionList);
        };
    }

    /**
     * Generates a plain text string representation of a list of KafkaTopicDescription objects.
     *
     * @param kafkaTopicDescriptionList A list of KafkaTopicDescription objects to be represented as a string.
     * @return A plain text string representation of topics.
     */
    private static String getPlain(List<KafkaTopicDescription> kafkaTopicDescriptionList) {
        StringBuilder s = new StringBuilder();
        for (KafkaTopicDescription topic : kafkaTopicDescriptionList) {
            final String singleRecord = String.format("name:%s, partitions:%d, replicas:%d\n",
                topic.getName(), topic.getPartitionCount(), topic.getReplicaCount());
            s.append(singleRecord);
        }
        return s.toString();
    }

    /**
     * Generates a JSON string representation of a list of KafkaTopicDescription objects using Jackson's ObjectMapper.
     *
     * @param kafkaTopicDescriptionList A list of KafkaTopicDescription objects.
     * @return A JSON string representation of topics.
     * @throws JsonProcessingException If an error occurs during serialization to JSON.
     */
    private static String getJson(List<KafkaTopicDescription> kafkaTopicDescriptionList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(kafkaTopicDescriptionList);
    }
}
