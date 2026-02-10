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
public class Ssl {
    private String sslTruststoreCertificate;
    private String sslKeystoreKey;
    private String sslKeystoreCertificateChain;

    public String getSslTruststoreCertificate() {
        return sslTruststoreCertificate;
    }

    public void setSslTruststoreCertificate(String sslTruststoreCertificate) {
        this.sslTruststoreCertificate = sslTruststoreCertificate;
    }

    public String getSslKeystoreKey() {
        return sslKeystoreKey;
    }

    public void setSslKeystoreKey(String sslKeystoreKey) {
        this.sslKeystoreKey = sslKeystoreKey;
    }

    public String getSslKeystoreCertificateChain() {
        return sslKeystoreCertificateChain;
    }

    public void setSslKeystoreCertificateChain(String sslKeystoreCertificateChain) {
        this.sslKeystoreCertificateChain = sslKeystoreCertificateChain;
    }

    public List<EnvVar> getSslEnvVar() {
        List<EnvVar> envVars = new ArrayList<>();

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.CA_CRT_ENV, this.getSslTruststoreCertificate());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.USER_KEY_ENV, this.getSslKeystoreKey());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.USER_CRT_ENV, this.getSslKeystoreCertificateChain());

        return envVars;
    }
}
