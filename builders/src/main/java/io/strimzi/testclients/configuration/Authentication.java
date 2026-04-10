/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling authentication related configuration.
 * Here we can configure everything related to OAuth, TLS, SCRAM-SHA.
 */
@Buildable(editableEnabled = false)
public class Authentication {
    /**
     * Common configuration
     */
    private String securityProtocol;

    private OAuth oauth;
    private Sasl sasl;
    private Ssl ssl;

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public void setOauth(OAuth oauth) {
        this.oauth = oauth;
    }

    public Sasl getSasl() {
        return sasl;
    }

    public void setSasl(Sasl sasl) {
        this.sasl = sasl;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public List<EnvVar> getAuthenticationEnvVars() {
        List<EnvVar> envVars = new ArrayList<>();

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.SECURITY_PROTOCOL_ENV, this.getSecurityProtocol());

        if (oauth != null && !oauth.getOAuthEnvVars().isEmpty()) {
            envVars.addAll(oauth.getOAuthEnvVars());
        }
        if (sasl != null && !sasl.getSaslEnvVars().isEmpty()) {
            envVars.addAll(sasl.getSaslEnvVars());
        }
        if (ssl != null && !ssl.getSslEnvVars().isEmpty()) {
            envVars.addAll(ssl.getSslEnvVars());
        }

        return envVars;
    }
}
