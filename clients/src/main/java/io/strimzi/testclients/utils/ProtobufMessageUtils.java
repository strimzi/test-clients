/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import com.google.protobuf.util.JsonFormat;
import io.apicurio.registry.serde.protobuf.ProtobufKafkaSerializer;
import io.apicurio.registry.serde.protobuf.ProtobufSchemaParser;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.kafka.KafkaProducerConfiguration;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

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
            final byte[] protoSchemaBytes = fetchSchema(configuration.getAdditionalConfig());
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
     * Fetches the raw Protobuf schema bytes from Apicurio Registry .
     *
     * @param config    producer additional config containing Apicurio Registry connection properties
     *
     * @return  raw bytes of the Protobuf schema
     * @throws IOException  if the schema cannot be fetched from the registry
     */
    private static byte[] fetchSchema(final Properties config) throws IOException {
        final URL url = buildSchemaUrl(config);
        try (final InputStream inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Builds the Apicurio Registry schema content URL based on the producer additional config.
     * URL format differs between API versions:
     * <ul>
     *   <li>v2: {@code /apis/registry/v2/groups/{groupId}/artifacts/{artifactId}/versions/{version}}</li>
     *   <li>v3: {@code /apis/registry/v3/groups/{groupId}/artifacts/{artifactId}/versions/{version}/content}</li>
     * </ul>
     *
     * @param config    producer additional config containing Apicurio Registry connection properties
     *
     * @return  URL pointing to the schema content in Apicurio Registry
     * @throws MalformedURLException        if the constructed URL is invalid
     * @throws IllegalArgumentException     if the specified API version is not supported
     */
    private static URL buildSchemaUrl(final Properties config) throws MalformedURLException {
        final String registryUrl = config.getProperty(ConfigurationConstants.REGISTRY_URL);
        final String groupId = config.getProperty(ConfigurationConstants.REGISTRY_GROUP_ID, ConfigurationConstants.DEFAULT_GROUP_ID);
        final String artifactId = config.getProperty(ConfigurationConstants.REGISTRY_ARTIFACT_ID);
        final String version = config.getProperty(ConfigurationConstants.REGISTRY_ARTIFACT_VERSION, ConfigurationConstants.REGISTRY_DEFAULT_ARTIFACT_VERSION);
        final String apiVersion = config.getProperty(ConfigurationConstants.REGISTRY_API_VERSION, ConfigurationConstants.REGISTRY_DEFAULT_API_VERSION);

        final String urlString;
        if (ConfigurationConstants.APICURIO_API_V2.equals(apiVersion)) {
            urlString = String.format("%s/groups/%s/artifacts/%s/versions/%s",
                registryUrl, groupId, artifactId, version);
        } else if (ConfigurationConstants.APICURIO_API_V3.equals(apiVersion)) {
            urlString = String.format("%s/groups/%s/artifacts/%s/versions/%s/content",
                registryUrl, groupId, artifactId, version);
        } else {
            throw new IllegalArgumentException("Unsupported Apicurio Registry API version: " + apiVersion + ". Supported versions are: v2, v3");
        }

        return URI.create(urlString).toURL();
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
