/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.configure;

import io.strimzi.testclients.arguments.CommandInterface;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Subcommand for handling common configuration of the admin-client
 * Users can either:
 *      - specify the bootstrap servers to not do --bootstrap-servers call in each execution
 *      - specify file path where the configuration is stored (so it will be loaded and copied to the config folder)
 *      - load the configuration from environment variables as it is done for other clients
 * This is useful in cases that the admin-client CLI tool is in some container with specified environmental variables or with mounted
 * configuration file, so users don't need to configure it using multiple calls
 */
@CommandLine.Command(name = "common")
public class CommonCommand implements CommandInterface {

    @CommandLine.ArgGroup(multiplicity = "1")
    Source source;

    static class Source {
        @CommandLine.Option(names = "--bootstrap-server", description = "Bootstrap server address")
        String bootstrapServer;

        @CommandLine.Option(names = "--from-file", description = "Load security configuration from file")
        String filePath;

        @CommandLine.Option(names = "--from-env", description = "Load configuration from environmental variables")
        boolean fromEnv;
    }

    @Override
    public Integer call() {
        if (source.fromEnv) {
            return loadConfigFromEnv();
        } else if (source.filePath != null) {
            return loadConfigFromFile();
        } else if (source.bootstrapServer != null) {
            return setBootstrapServer();
        }

        return 0;
    }

    /**
     * Sets the bootstrap servers property inside config.properties file in admin-client's config folder
     * @return status code of the operation
     */
    private Integer setBootstrapServer() {
        Properties properties = new Properties();

        properties.put(ConfigurationUtils.transformPropertyToPropertiesFormat(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), source.bootstrapServer);
        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }

    /**
     * Loads configuration from environmental variables based on specified env vars in {@link ConfigurationConstants}
     * Then it stores the configuration in config.properties file in admin-client's config folder
     * @return status code of the operation
     */
    private Integer loadConfigFromEnv() {
        Properties properties = transformAndFilterPropertiesFromSource(System.getenv());
        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }

    /**
     * Loads configuration from file on the file path.
     * Then it stores the configuration in config.properties file in admin-client's config folder
     * @return status code of the operation
     */
    private Integer loadConfigFromFile() {
        ConfigurationUtils.writeToConfigurationFile(ConfigurationUtils.getPropertiesFromConfigurationFile(source.filePath));
        return 0;
    }

    /**
     * Method which transforms all keys to the "properties format" and filters those options, that are related to the
     * admin-client
     * @param source Map with the key x value pairs from the source (f.e. environment variables)
     * @return filtered and transformed Properties
     */
    private Properties transformAndFilterPropertiesFromSource(Map<String, String> source) {
        Properties properties = new Properties();

        // transform all keys in source map to properties style
        source = source.entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> ConfigurationUtils.transformPropertyToPropertiesFormat(entry.getKey()), Map.Entry::getValue));

        // filter all entries for expected properties and put it into the Properties
        source.entrySet()
            .stream()
            .filter(entry -> ConfigurationConstants.BASIC_PROPERTY_LIST.contains(entry.getKey()))
            .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

        return properties;
    }
}
