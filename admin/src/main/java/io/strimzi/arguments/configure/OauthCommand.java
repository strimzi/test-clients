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

/**
 * Sub-command for configuring OAuth related configuration for admin-client
 */
@CommandLine.Command(name = "oauth")
public class OauthCommand implements CommandInterface {
    @CommandLine.Option(names = {"--client-id", "-id"}, description = "ID of the client used in Oauth")
    String oauthClientId;

    @CommandLine.Option(names = {"--client-secret", "-cs"}, description = "Secret of the client used in Oauth")
    String oauthClientSecret;

    @CommandLine.Option(names = {"--access-token", "-at"}, description = "Access token for accessing Oauth")
    String oauthAccessToken;

    @CommandLine.Option(names = {"--refresh-token", "-rt"}, description = "Oauth refresh token")
    String oauthRefreshToken;

    @CommandLine.Option(names = {"--token-endpoint", "-teu"}, description = "Oauth token endpoint URI for obtaining new token")
    String oauthTokenEndpointUri;


    @Override
    public Integer call() {
        return setProperties();
    }

    private Integer setProperties() {
        Properties properties = new Properties();

        properties.put(ConfigurationConstants.OAUTH_CLIENT_ID_PROPERTY, oauthClientId);
        properties.put(ConfigurationConstants.OAUTH_CLIENT_SECRET_PROPERTY, oauthClientSecret);
        properties.put(ConfigurationConstants.OAUTH_ACCESS_TOKEN_PROPERTY, oauthAccessToken);
        properties.put(ConfigurationConstants.OAUTH_REFRESH_TOKEN_PROPERTY, oauthRefreshToken);
        properties.put(ConfigurationConstants.OAUTH_TOKEN_ENDPOINT_URI_ENV, oauthTokenEndpointUri);

        ConfigurationUtils.writeToConfigurationFile(properties);
        return 0;
    }
}
