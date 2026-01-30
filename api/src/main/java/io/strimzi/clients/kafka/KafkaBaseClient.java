/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.clients.kafka;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.strimzi.configuration.ConfigurationConstants;
import io.strimzi.configuration.Environment;
import io.sundr.builder.annotations.Buildable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Buildable
public class KafkaBaseClient extends KafkaCommon {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name of the client cannot be empty");
        }
        this.name = name;
    }

    Job getClientJob(List<EnvVar> clientSpecificEnvVars) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", this.getName());

        PodSpecBuilder podSpecBuilder = new PodSpecBuilder();

        if (this.getImage().getImagePullSecret() != null && !this.getImage().getImagePullSecret().isEmpty()) {
            podSpecBuilder.withImagePullSecrets(new LocalObjectReference(this.getImage().getImagePullSecret()));
        }

        List<EnvVar> envVars = new ArrayList<>(clientSpecificEnvVars);

        // Add all the additional EnvVars to the List
        if (this.getAdditionalEnvVars() != null) {
            envVars.addAll(this.getAdditionalEnvVars());
        }

        // Add EnvVars for tracing if configured
        if (this.getTracing() != null && !this.getTracing().getTracingEnvVars().isEmpty()) {
            envVars.addAll(this.getTracing().getTracingEnvVars());
        }

        if (this.getOauth() != null && !this.getOauth().getOAuthEnvVars().isEmpty()) {
            envVars.addAll(this.getOauth().getOAuthEnvVars());
        }

        if (this.getSasl() != null && !this.getSasl().getSaslEnvVars().isEmpty()) {
            envVars.addAll(this.getSasl().getSaslEnvVars());
        }

        if (this.getSsl() != null && !this.getSsl().getSslEnvVar().isEmpty()) {
            envVars.addAll(this.getSsl().getSslEnvVar());
        }

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.BOOTSTRAP_SERVERS_ENV, this.getBootstrapAddress());
        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.ADDITIONAL_CONFIG_ENV, this.getAdditionalConfig());

        return new JobBuilder()
            .withNewMetadata()
                .withNamespace(this.getNamespaceName())
                .withLabels(labels)
                .withName(this.getName())
            .endMetadata()
            .withNewSpec()
                .withBackoffLimit(0)
                .withNewTemplate()
                    .withNewMetadata()
                        .withLabels(labels)
                    .endMetadata()
                    .withNewSpecLike(podSpecBuilder.build())
                        .withRestartPolicy("Never")
                        .addNewContainer()
                            .withName(this.getName())
                            .withImagePullPolicy(this.getImage().getImagePullPolicy())
                            .withImage(this.getImage().getImageName())
                            .addAllToEnv(envVars)
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
    }
}
