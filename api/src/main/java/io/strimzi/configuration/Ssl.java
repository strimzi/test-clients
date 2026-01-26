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

        if (this.getSslTruststoreCertificate() != null && !this.getSslTruststoreCertificate().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("CA_CRT")
                .withValue(this.getSslTruststoreCertificate())
                .build()
            );
        }

        if (this.getSslKeystoreKey() != null && !this.getSslKeystoreKey().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("USER_KEY")
                .withValue(this.getSslKeystoreKey())
                .build()
            );
        }

        if (this.getSslKeystoreCertificateChain() != null && !this.getSslKeystoreCertificateChain().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName("USER_CRT")
                .withValue(this.getSslKeystoreCertificateChain())
                .build()
            );
        }

        return envVars;
    }
}
