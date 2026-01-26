/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

import java.util.List;

public class Environment {

    public static void configureEnvVariableOrSkip(List<EnvVar> envVarList, String envVariableName, Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            envVarList.add(new EnvVarBuilder()
                .withName(envVariableName)
                .withValue(String.valueOf(value))
                .build()
            );
        }
    }
}
