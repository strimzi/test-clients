/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.strimzi.testclients.configuration.kafka.KafkaProducerConfiguration;

public class JsonMessgeUtils {

    private JsonMessgeUtils() {}

    /**
     * Parses the configured message payload into a {@link JsonNode}.
     *
     * <p>This method reads the raw JSON string and converts it
     * into a Jackson tree model representation.</p>
     *
     * @param configuration the Kafka producer configuration containing
     *                      the JSON message payload
     * @return the parsed {@link JsonNode}
     * @throws RuntimeException if the message cannot be parsed as valid JSON
     */
    public static JsonNode buildMessageFromJson(final KafkaProducerConfiguration configuration) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(configuration.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to build JSON Schema message", e);
        }
    }
}
