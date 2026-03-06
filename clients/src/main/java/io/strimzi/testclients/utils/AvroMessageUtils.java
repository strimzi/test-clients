/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.strimzi.testclients.configuration.kafka.KafkaProducerConfiguration;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AvroMessageUtils {
    private AvroMessageUtils() {}

    /**
     * Builds an Avro {@link GenericRecord} from a JSON message using
     * the schema provided in the producer configuration.
     *
     * This method:
     * <ul>
     *     <li>Fetches the Avro schema from the additional producer configuration.</li>
     *     <li>Parses the schema definition.</li>
     *     <li>Deserializes the JSON message into a map of field values.</li>
     *     <li>Populates a {@link GenericRecordBuilder} with the parsed fields.</li>
     * </ul>
     *
     * @param configuration the Kafka producer configuration containing the
     *                      message payload and schema configuration
     * @return a {@link GenericRecord} constructed from the JSON message
     * @throws RuntimeException if schema parsing or message conversion fails
     */
    public static GenericRecord buildMessageFromJson(final KafkaProducerConfiguration configuration) {
        try {
            final byte[] avroSchemaBytes = MessageUtils.fetchSchema(configuration.getAdditionalConfig());
            final Schema schema = new Schema.Parser().parse(new String(avroSchemaBytes, StandardCharsets.UTF_8));

            final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
            final ObjectMapper mapper = new ObjectMapper();
            final Map<String, Object> fields = mapper.readValue(configuration.getMessage(), Map.class);

            fields.forEach(builder::set);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Avro message", e);
        }
    }
}
