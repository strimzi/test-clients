/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.List;

@Buildable
public class Sasl {
    private String saslMechanism;
    private String saslJaasConfig;
    private String saslUserName;
    private String saslPassword;

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }

    public void setSaslJaasConfig(String saslJaasConfig) {
        this.saslJaasConfig = saslJaasConfig;
    }

    public String getSaslUserName() {
        return saslUserName;
    }

    public void setSaslUserName(String saslUserName) {
        this.saslUserName = saslUserName;
    }

    public String getSaslPassword() {
        return saslPassword;
    }

    public void setSaslPassword(String saslPassword) {
        this.saslPassword = saslPassword;
    }

    public List<EnvVar> getSaslEnvVars() {
        List<EnvVar> envVars = new ArrayList<>();

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.SASL_MECHANISM_ENV, this.getSaslMechanism());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.SASL_JAAS_CONFIG_ENV, this.getSaslJaasConfig());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.USER_NAME_ENV, this.getSaslUserName());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.USER_PASSWORD_ENV, this.getSaslPassword());

        return envVars;
    }
}
