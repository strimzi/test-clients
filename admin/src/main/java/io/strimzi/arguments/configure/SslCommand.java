/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.configure;

import io.strimzi.arguments.CommandInterface;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.constants.Constants;
import io.strimzi.utils.ConfigurationUtils;
import picocli.CommandLine;

import java.util.Properties;

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
