/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import io.strimzi.SaslType;
import io.strimzi.arguments.CommandInterface;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Properties;

/**
 * Subcommand for setting up SASL related configuration of admin-client
 * Users can either set "SASL JAAS configuration" or "username & password" for the client
 */
@CommandLine.Command(name = "sasl")
public class SaslCommand implements CommandInterface {

    @CommandLine.Option(names = "--mechanism", description = "SASL mechanism", required = true, type = SaslType.class)
    SaslType saslMechanism;

    @CommandLine.ArgGroup(multiplicity = "1")
    Configuration configuration;

    static class Configuration {
        @CommandLine.Option(names = "--jaas-config", description = "SASL JAAS config that should be passed to the configuration", required = true)
        String saslJaasConfig;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
        User user;
    }

    static class User {
        @CommandLine.Option(names = "--username", description = "Name of the user", required = true)
        String saslUserName;

        @CommandLine.Option(names = "--password", description = "Password of the user", required = true)
        String saslPassword;
    }

    @Override
    public Integer call() {
        return setProperties();
    }

    private Integer setProperties() {
        Properties properties = new Properties();

        properties.put(ConfigurationConstants.SASL_MECHANISM_PROPERTY, saslMechanism.toString());

        if (configuration.saslJaasConfig != null) {
            properties.put(ConfigurationConstants.SASL_JAAS_CONFIG_PROPERTY, configuration.saslJaasConfig);
        } else {
            properties.put(ConfigurationConstants.USER_NAME_PROPERTY, configuration.user.saslUserName);
            properties.put(ConfigurationConstants.USER_PASSWORD_PROPERTY, configuration.user.saslPassword);
        }

        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }
}
