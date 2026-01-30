/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

@Buildable
public class OAuth {
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthAccessToken;
    private String oauthRefreshToken;
    private String oauthTokenEndpointUri;

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public void setOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
    }

    public String getOauthAccessToken() {
        return oauthAccessToken;
    }

    public void setOauthAccessToken(String oauthAccessToken) {
        this.oauthAccessToken = oauthAccessToken;
    }

    public String getOauthRefreshToken() {
        return oauthRefreshToken;
    }

    public void setOauthRefreshToken(String oauthRefreshToken) {
        this.oauthRefreshToken = oauthRefreshToken;
    }

    public String getOauthTokenEndpointUri() {
        return oauthTokenEndpointUri;
    }

    public void setOauthTokenEndpointUri(String oauthTokenEndpointUri) {
        this.oauthTokenEndpointUri = oauthTokenEndpointUri;
    }

    public List<EnvVar> getOAuthEnvVars() {
        List<EnvVar> envVars = new ArrayList<>();

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.OAUTH_CLIENT_ID_ENV, this.getOauthClientId());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.OAUTH_CLIENT_SECRET_ENV, this.getOauthClientSecret());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.OAUTH_ACCESS_TOKEN_ENV, this.getOauthAccessToken());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.OAUTH_REFRESH_TOKEN_ENV, this.getOauthRefreshToken());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.OAUTH_TOKEN_ENDPOINT_URI_ENV, this.getOauthTokenEndpointUri());

        return envVars;
    }
}
