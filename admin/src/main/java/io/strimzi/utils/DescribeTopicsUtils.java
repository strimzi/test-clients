/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.strimzi.models.KafkaTopicDescription;

import java.util.List;

public class DescribeTopicsUtils {

    public static String getOutput(OutputFormat outputFormat, List<KafkaTopicDescription> kafkaTopicDescriptionList) throws JsonProcessingException {
        return switch (outputFormat) {
            case PLAIN -> getPlain(kafkaTopicDescriptionList);
            case JSON -> getJson(kafkaTopicDescriptionList);
        };
    }

    private static String getPlain(List<KafkaTopicDescription> kafkaTopicDescriptionList) {
        StringBuilder s = new StringBuilder();
        for (KafkaTopicDescription topic : kafkaTopicDescriptionList) {
            final String singleRecord = String.format("name:%s, partitions:%d, replicas:%d\n",
                topic.getName(), topic.getPartitionCount(), topic.getReplicaCount());
            s.append(singleRecord);
        }
        return s.toString();
    }

    private static String getJson(List<KafkaTopicDescription> kafkaTopicDescriptionList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(kafkaTopicDescriptionList);
    }


}
