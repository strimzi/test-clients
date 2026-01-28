/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.strimzi.configuration.Image;
import io.strimzi.configuration.OAuth;
import io.strimzi.configuration.Sasl;
import io.strimzi.configuration.Ssl;
import io.strimzi.configuration.Tracing;

import java.util.List;

class KafkaCommon {
    private String namespaceName;

    private String bootstrapAddress;

    private Image image = new Image();
    private OAuth oauth;
    private Sasl sasl;
    private Ssl ssl;
    private Tracing tracing;

    private List<EnvVar> additionalEnvVars;
    private String additionalConfig;

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        if (namespaceName == null) {
            throw new IllegalArgumentException("Name of Namespace cannot be empty");
        }
        this.namespaceName = namespaceName;
    }

    public String getBootstrapAddress() {
        return bootstrapAddress;
    }

    public void setBootstrapAddress(String bootstrapAddress) {
        if (bootstrapAddress == null) {
            throw new IllegalArgumentException("Bootstrap address cannot be empty");
        }
        this.bootstrapAddress = bootstrapAddress;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
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

    public Tracing getTracing() {
        return tracing;
    }

    public void setTracing(Tracing tracing) {
        this.tracing = tracing;
    }

    public List<EnvVar> getAdditionalEnvVars() {
        return additionalEnvVars;
    }

    public void setAdditionalEnvVars(List<EnvVar> additionalEnvVars) {
        this.additionalEnvVars = additionalEnvVars;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }
}
