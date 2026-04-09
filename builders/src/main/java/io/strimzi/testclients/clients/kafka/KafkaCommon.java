/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.strimzi.testclients.configuration.Authentication;
import io.strimzi.testclients.configuration.Image;
import io.strimzi.testclients.configuration.Tracing;

import java.util.List;

class KafkaCommon {
    private String namespaceName;

    private String bootstrapAddress;

    private Image image = new Image();
    private Authentication authentication;
    private Tracing tracing;

    private List<EnvVar> additionalEnvVars;
    private String additionalConfig;

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        if (namespaceName == null || namespaceName.isEmpty()) {
            throw new IllegalArgumentException("Name of Namespace cannot be empty");
        }
        this.namespaceName = namespaceName;
    }

    public String getBootstrapAddress() {
        return bootstrapAddress;
    }

    public void setBootstrapAddress(String bootstrapAddress) {
        if (bootstrapAddress == null || bootstrapAddress.isEmpty()) {
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

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
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
