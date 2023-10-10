/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import io.strimzi.arguments.CommandInterface;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class CommonCommand implements CommandInterface {

    @CommandLine.Option(names = "--bootstrap-server", description = "Bootstrap server address")
    String bootstrapServer;

    @CommandLine.ArgGroup(multiplicity = "1")
    Source source;

    static class Source {
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
        } else if (bootstrapServer != null) {
            return setBootstrapServer();
        }

        return 0;
    }

    private Integer setBootstrapServer() {
        Properties properties = new Properties();

        properties.put(ConfigurationUtils.transformPropertyToPropertiesFormat(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV), bootstrapServer);
        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }

    private Integer loadConfigFromEnv() {
        Properties properties = loadConfigurationToProperties(System.getenv());
        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }

    private Integer loadConfigFromFile() {
        ConfigurationUtils.writeToConfigurationFile(ConfigurationUtils.getPropertiesFromConfigurationFile(source.filePath));
        return 0;
    }

    private Properties loadConfigurationToProperties(Map<String, String> source) {
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
