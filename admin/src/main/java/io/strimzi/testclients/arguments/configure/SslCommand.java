/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.configure;

import io.strimzi.testclients.arguments.CommandInterface;
import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.constants.Constants;
import io.strimzi.testclients.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Properties;

/**
 * Subcommand for setting up SSL related configuration of admin-client
 */
@CommandLine.Command(name = "ssl")
public class SslCommand implements CommandInterface {

    @CommandLine.Option(names = "--truststore", description = "File path to SSL truststore certificate")
    String truststorePath;

    @CommandLine.Option(names = "--keystore-key", description = "File path to SSL keystore key")
    String keystoreKeyPath;

    @CommandLine.Option(names = "--keystore-cert", description = "File path to SSL keystore certificate chain")
    String keystoreCertPath;

    @Override
    public Integer call() {
        return copyCertificates();
    }

    /**
     * Method that copies certificates from the file paths specified by users to the configuration folder
     * The name of the certificates (files) are then prefixed with "@" in the properties file, so when
     * the configuration file will be loaded by admin-client, it can load the certificates and pass it to the
     * {@link io.strimzi.testclients.configuration.kafka.KafkaClientsConfiguration} instance
     * @return status code of the operation
     */
    private Integer copyCertificates() {
        Properties properties = new Properties();

        if (truststorePath != null) {
            ConfigurationUtils.copyFileToConfigurationFolder(truststorePath, Constants.TRUSTSTORE_FILE_NAME);
            properties.put(ConfigurationConstants.CA_CRT_PROPERTY, "@" + Constants.TRUSTSTORE_FILE_NAME);
        }

        if (keystoreKeyPath != null) {
            ConfigurationUtils.copyFileToConfigurationFolder(keystoreKeyPath, Constants.KEYSTORE_KEY_FILE_NAME);
            properties.put(ConfigurationConstants.USER_KEY_PROPERTY, "@" + Constants.KEYSTORE_KEY_FILE_NAME);
        }

        if (keystoreCertPath != null) {
            ConfigurationUtils.copyFileToConfigurationFolder(keystoreCertPath, Constants.KEYSTORE_CERT_FILE_NAME);
            properties.put(ConfigurationConstants.USER_CRT_PROPERTY, "@" + Constants.KEYSTORE_CERT_FILE_NAME);
        }

        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }
}
