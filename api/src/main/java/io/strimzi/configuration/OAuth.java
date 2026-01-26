/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
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

        if (this.getOauthClientId() != null && !this.getOauthClientId().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("OAUTH_CLIENT_ID")
                .withValue(this.getOauthClientId())
                .build()
            );
        }

        if (this.getOauthClientSecret() != null && !this.getOauthClientSecret().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("OAUTH_CLIENT_SECRET")
                .withValue(this.getOauthClientSecret())
                .build()
            );
        }

        if (this.getOauthAccessToken() != null && !this.getOauthAccessToken().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("OAUTH_ACCESS_TOKEN")
                .withValue(this.getOauthAccessToken())
                .build()
            );
        }

        if (this.getOauthRefreshToken() != null && !this.getOauthRefreshToken().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("OAUTH_REFRESH_TOKEN")
                .withValue(this.getOauthRefreshToken())
                .build()
            );
        }

        if (this.getOauthTokenEndpointUri() != null && !this.getOauthTokenEndpointUri().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("OAUTH_TOKEN_ENDPOINT_URI")
                .withValue(this.getOauthTokenEndpointUri())
                .build()
            );
        }

        return envVars;
    }
}
