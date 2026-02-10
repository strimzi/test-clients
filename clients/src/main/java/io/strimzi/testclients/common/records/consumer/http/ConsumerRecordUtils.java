/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.common.records.consumer.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsumerRecordUtils {

    private static final Logger LOGGER = LogManager.getLogger(ConsumerRecordUtils.class);

    public static ConsumerRecord[] parseConsumerRecordsFromJson(String response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        return objectMapper.readValue(response, ConsumerRecord[].class);
    }

    public static void logConsumerRecords(ConsumerRecord[] records) {
        for (ConsumerRecord record : records) {
            LOGGER.info(record.toString());
        }
    }
}
