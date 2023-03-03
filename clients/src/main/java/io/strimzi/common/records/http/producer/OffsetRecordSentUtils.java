/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.records.http.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OffsetRecordSentUtils {

    private static final Logger LOGGER = LogManager.getLogger(OffsetRecordSentUtils.class);

    public static void logOffsetRecordsSent(OffsetRecordSent[] offsetRecordsSent) {
        for (OffsetRecordSent offsetRecordSent : offsetRecordsSent) {
            LOGGER.info(offsetRecordSent.toString());
        }
    }

    public static OffsetRecordSent[] parseOffsetRecordsSent(String response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode json = objectMapper.readTree(response);
        String offsets = json.get("offsets").toString();

        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        return objectMapper.readValue(offsets, OffsetRecordSent[].class);
    }
}
