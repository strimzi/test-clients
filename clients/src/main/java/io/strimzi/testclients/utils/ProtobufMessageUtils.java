/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.google.protobuf.util.JsonFormat;
import io.apicurio.registry.serde.protobuf.ProtobufKafkaSerializer;
import io.apicurio.registry.serde.protobuf.ProtobufSchemaParser;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import io.strimzi.testclients.configuration.kafka.KafkaProducerConfiguration;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

import java.util.Collections;

/**
 * Utility class for building Protobuf {@link DynamicMessage} instances from JSON strings.
 * Fetches the Protobuf schema from Apicurio Registry and uses it to parse the JSON message
 * into a {@link DynamicMessage} that can be passed to {@link ProtobufKafkaSerializer}.
 *
 * Supports Apicurio Registry API v2 and v3, controlled via {@code apicurio.registry.api-version}
 * in the producer additional config. Defaults to v3 if not specified.
 */
public class ProtobufMessageUtils {
    private ProtobufMessageUtils() {}

    /**
     * Builds a {@link DynamicMessage} from a JSON string using the Protobuf schema
     * fetched from Apicurio Registry. Schema location is determined by the producer
     * additional config properties ({@code apicurio.registry.url}, {@code apicurio.registry.group-id},
     * {@code apicurio.registry.artifact-id}, {@code apicurio.registry.artifact-version}).
     *
     * @param configuration     Kafka producer configuration containing the JSON message
     *                          and Apicurio Registry connection properties
     *
     * @return  {@link DynamicMessage} built from the JSON message and fetched schema
     * @throws RuntimeException     if the schema cannot be fetched or the message cannot be parsed
     */
    public static DynamicMessage buildMessageFromJson(final KafkaProducerConfiguration configuration) {
        try {
            final byte[] protoSchemaBytes = MessageUtils.fetchSchema(configuration.getAdditionalConfig());
            final Descriptor descriptor = parseDescriptor(protoSchemaBytes);

            // Build DynamicMessage using descriptor defining the message structure
            final DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
            JsonFormat.parser().ignoringUnknownFields().merge(configuration.getMessage(), builder);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Protobuf message", e);
        }
    }


    /**
     * Parses raw Protobuf schema bytes into a {@link Descriptor} representing
     * the first message type defined in the schema.
     *
     * @param protoSchemaBytes  raw bytes of the Protobuf schema
     *
     * @return  {@link Descriptor} of the first message type in the schema
     */
    private static Descriptor parseDescriptor(final byte[] protoSchemaBytes) {
        final ProtobufSchemaParser<DynamicMessage> parser = new ProtobufSchemaParser<>();
        final ProtobufSchema parsedSchema = parser.parseSchema(protoSchemaBytes, Collections.emptyMap());
        return parsedSchema.getFileDescriptor().getMessageTypes().getFirst();
    }
}
