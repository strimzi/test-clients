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
public class Tracing {
    private String serviceName;
    private String serviceNameEnvVar;
    private String tracingType;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceNameEnvVar() {
        return serviceNameEnvVar;
    }

    public void setServiceNameEnvVar(String serviceNameEnvVar) {
        this.serviceNameEnvVar = serviceNameEnvVar;
    }

    public String getTracingType() {
        return tracingType;
    }

    public void setTracingType(String tracingType) {
        this.tracingType = tracingType;
    }

    public List<EnvVar> getTracingEnvVars() {
        List<EnvVar> envVars = new ArrayList<>();

        if (this.getServiceName() != null && !this.getServiceName().isEmpty()
            && this.getServiceNameEnvVar() != null && !this.getServiceNameEnvVar().isEmpty()) {
            envVars.add(new EnvVarBuilder()
                .withName(this.getServiceNameEnvVar())
                .withValue(this.getServiceName())
                .build()
            );
        }

        Environment.configureEnvVariableOrSkip(envVars, ConfigurationConstants.TRACING_TYPE_ENV, this.getTracingType());

        return envVars;
    }
}
