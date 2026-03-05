/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

import io.apicurio.registry.resolver.config.SchemaResolverConfig;
import io.strimzi.testclients.configuration.ConfigurationConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

public class MessageUtils {
    private MessageUtils() {}

    /**
     * Fetches the raw schema bytes from Apicurio Registry.
     *
     * <p>The schema type is not restricted and may represent any artifact
     * supported by the registry (only Avro, Protobuf).</p>
     *
     * @param config producer additional configuration containing
     *               Apicurio Registry connection properties
     *
     * @return raw bytes of the schema content
     * @throws IOException if the schema cannot be fetched from the registry
     */
    public static byte[] fetchSchema(final Properties config) throws IOException {
        final URL url = buildSchemaUrl(config);
        try (final InputStream inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Builds the Apicurio Registry schema content URL based on the producer configuration.
     *
     * <p>The URL format differs depending on the configured Apicurio API version:
     * <ul>
     *   <li>v2: {@code /groups/{groupId}/artifacts/{artifactId}/versions/{version}}</li>
     *   <li>v3: {@code /groups/{groupId}/artifacts/{artifactId}/versions/{version}/content}</li>
     * </ul>
     * </p>
     *
     * <p>This method is schema-type agnostic and works for any artifact stored
     * in Apicurio Registry.</p>
     *
     * @param config producer additional configuration containing
     *               Apicurio Registry connection properties
     *
     * @return URL pointing to the schema content in Apicurio Registry
     * @throws MalformedURLException    if the constructed URL is invalid
     * @throws IllegalArgumentException if the specified API version is not supported
     */
    private static URL buildSchemaUrl(final Properties config) throws MalformedURLException {
        final String registryUrl = config.getProperty(SchemaResolverConfig.REGISTRY_URL);
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
}
