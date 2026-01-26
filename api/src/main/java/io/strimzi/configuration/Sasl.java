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

        if (this.getSaslMechanism() != null && !this.getSaslMechanism().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("SASL_MECHANISM_ENV")
                .withValue(this.getSaslMechanism())
                .build()
            );
        }

        if (this.getSaslJaasConfig() != null && !this.getSaslJaasConfig().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("SASL_JAAS_CONFIG")
                .withValue(this.getSaslJaasConfig())
                .build()
            );
        }

        if (this.getSaslUserName() != null && !this.getSaslUserName().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("USER_NAME")
                .withValue(this.getSaslUserName())
                .build()
            );
        }

        if (this.getSaslPassword() != null && !this.getSaslPassword().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("USER_PASSWORD")
                .withValue(this.getSaslPassword())
                .build()
            );
        }

        return envVars;
    }
}
