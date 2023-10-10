/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import io.strimzi.arguments.CommandInterface;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Properties;

public class SaslCommand implements CommandInterface {

    @CommandLine.Option(names = "--mechanism", description = "SASL mechanism", required = true)
    String saslMechanism;

    @CommandLine.Option(names = "--jaas-config", description = "SASL JAAS config that should be passed to the configuration", required = true)
    String saslJaasConfig;

    @CommandLine.Option(names = "--user", description = "Username of the user", required = true)
    String saslUserName;

    @CommandLine.Option(names = "--password", description = "Password of the user", required = true)
    String saslPassword;

    @Override
    public Integer call() {
        return setProperties();
    }

    private Integer setProperties() {
        Properties properties = new Properties();

        properties.put(ConfigurationConstants.SASL_MECHANISM_PROPERTY, saslMechanism);
        properties.put(ConfigurationConstants.SASL_JAAS_CONFIG_PROPERTY, saslJaasConfig);
        properties.put(ConfigurationConstants.USER_NAME_PROPERTY, saslUserName);
        properties.put(ConfigurationConstants.USER_PASSWORD_PROPERTY, saslPassword);

        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }
}
