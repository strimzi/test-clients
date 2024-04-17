/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin;

import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.configuration.kafka.KafkaClientsConfiguration;
import io.strimzi.properties.BasicKafkaProperties;
import io.strimzi.utils.ConfigurationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class AdminProperties {

    /**
     * Create Properties from the configuration file and {@param bootstrapServer}.
     * The Properties will be used in KafkaAdmin client initialization.
     * @param bootstrapServer of Kafka
     * @return Kafka Properties
     */
    public static Properties adminProperties(String bootstrapServer) {
        Properties properties = transformAllLoadedProperties(ConfigurationUtils.getAdminClientPropertiesIfExists());

        // list of fields that can contain reference to file
        List<String> sslFields = List.of(
            ConfigurationConstants.CA_CRT_ENV,
            ConfigurationConstants.USER_KEY_ENV,
            ConfigurationConstants.USER_CRT_ENV
        );

        Map<String, String> propertiesInMap = new HashMap<>();

        // create Map from the Properties, also get contents of the files and paste it to the Map
        properties.forEach((k, v) -> {
            String key = String.valueOf(k);
            String value = String.valueOf(v);

            if (sslFields.contains(key) && value.startsWith("@")) {
                value = ConfigurationUtils.getContentsOfTheFileInConfigFolder(value);
            }

            propertiesInMap.put(key, value);
        });

        // in case that bootstrap server is not specified in the configuration file, add it from the variable
        // when the configuration file will not contain the field and variable will be null, exception will be thrown in the KafkaClientsConfiguration
        if (!propertiesInMap.containsKey(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV) || bootstrapServer != null) {
            propertiesInMap.put(ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, bootstrapServer);
        }

        KafkaClientsConfiguration configuration = new KafkaClientsConfiguration(propertiesInMap);

        // return Properties passed directly to the KafkaAdmin client
        return BasicKafkaProperties.clientProperties(configuration);
    }

    /**
     * Transforms all keys in the properties from the configuration file to "env variable format"
     * @param properties from configuration file
     * @return updated properties with all keys in env variable format
     */
    private static Properties transformAllLoadedProperties(Properties properties) {
        Properties updatedProps = new Properties();

        updatedProps.putAll(
            properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> ConfigurationUtils.transformPropertyToEnvFormat(String.valueOf(entry.getKey())), Map.Entry::getValue))
        );

        return updatedProps;
    }
}
