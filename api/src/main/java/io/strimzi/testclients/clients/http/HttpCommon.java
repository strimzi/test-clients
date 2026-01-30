/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.clients.http;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.strimzi.testclients.configuration.Image;
import io.strimzi.testclients.configuration.Tracing;

import java.util.List;

class HttpCommon {
    private String namespaceName;

    private String hostname;
    private Integer port;
    private String endpointPrefix;

    private String topicName;
    private Long messageCount = 100L;
    private String messageType;

    private String sslTruststoreCertificate;
    private Tracing tracing;
    private Image image = new Image();
    private List<EnvVar> additionalEnvVars;

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        if (namespaceName == null || namespaceName.isEmpty()) {
            throw new IllegalArgumentException("Name of Namespace cannot be empty");
        }
        this.namespaceName = namespaceName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("Hostname cannot be empty");
        }
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        if (port == null) {
            throw new IllegalArgumentException("Port cannot be empty");
        }
        this.port = port;
    }

    public String getEndpointPrefix() {
        return endpointPrefix;
    }

    public void setEndpointPrefix(String endpointPrefix) {
        this.endpointPrefix = endpointPrefix;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("Name of Topic cannot be empty");
        }

        this.topicName = topicName;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSslTruststoreCertificate() {
        return sslTruststoreCertificate;
    }

    public void setSslTruststoreCertificate(String sslTruststoreCertificate) {
        this.sslTruststoreCertificate = sslTruststoreCertificate;
    }

    public Tracing getTracing() {
        return tracing;
    }

    public void setTracing(Tracing tracing) {
        this.tracing = tracing;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<EnvVar> getAdditionalEnvVars() {
        return additionalEnvVars;
    }

    public void setAdditionalEnvVars(List<EnvVar> additionalEnvVars) {
        this.additionalEnvVars = additionalEnvVars;
    }
}
