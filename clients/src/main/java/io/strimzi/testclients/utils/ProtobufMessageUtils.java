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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private static final Logger LOGGER = LogManager.getLogger(ProtobufMessageUtils.class);

    private ProtobufMessageUtils() {}

    // Internal config to determine Apicurio API version
    private static final String REGISTRY_API_VERSION = "apicurio.registry.api-version";

    // Apicurio registry configuration keys
    private static final String REGISTRY_URL = "apicurio.registry.url";
    private static final String REGISTRY_GROUP_ID = "apicurio.registry.group-id";
    private static final String REGISTRY_ARTIFACT_ID = "apicurio.registry.artifact-id";
    private static final String REGISTRY_ARTIFACT_VERSION = "apicurio.registry.artifact-version";

    // Defaults
    private static final String DEFAULT_GROUP_ID = "default";
    private static final String DEFAULT_VERSION = "1";
    private static final String DEFAULT_API_VERSION = "v3";

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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fetched proto schema: [{}]", new String(protoSchemaBytes, StandardCharsets.UTF_8));
            }

            final Descriptor descriptor = parseDescriptor(protoSchemaBytes);
            LOGGER.info("Parsed descriptor: {}", descriptor.getName());

            // Build DynamicMessage using descriptor defining the message structure
            final DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
            JsonFormat.parser().ignoringUnknownFields().merge(configuration.getMessage(), builder);

            return builder.build();
        } catch (Exception e) {
            LOGGER.error("Failed to build Protobuf message", e);
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
        LOGGER.info("Fetching Protobuf schema from: {}", url);
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
        final String registryUrl = config.getProperty(REGISTRY_URL);
        final String groupId = config.getProperty(REGISTRY_GROUP_ID, DEFAULT_GROUP_ID);
        final String artifactId = config.getProperty(REGISTRY_ARTIFACT_ID);
        final String version = config.getProperty(REGISTRY_ARTIFACT_VERSION, DEFAULT_VERSION);
        final String apiVersion = config.getProperty(REGISTRY_API_VERSION, DEFAULT_API_VERSION);

        final String urlString;
        if ("v2".equals(apiVersion)) {
            urlString = String.format("%s/apis/registry/v2/groups/%s/artifacts/%s/versions/%s",
                registryUrl, groupId, artifactId, version);
        } else if ("v3".equals(apiVersion)) {
            urlString = String.format("%s/apis/registry/v3/groups/%s/artifacts/%s/versions/%s/content",
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
