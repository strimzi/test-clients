/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

import java.util.List;

public class Environment {

    /**
     * Method for creating EnvVar and then updating the list of EnvVars with it - in case that the value is not empty or `null`.
     *
     * @param envVarList        current list of EnvVariables.
     * @param envVariableName   name of the EnvVar.
     * @param value             value of the EnvVar.
     */
    public static void configureEnvVariableOrSkip(List<EnvVar> envVarList, String envVariableName, Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            envVarList.add(new EnvVarBuilder()
                .withName(envVariableName)
                .withValue(String.valueOf(value))
                .build()
            );
        }
    }

    /**
     * Method for creating EnvVar with `valueFrom` Secret - used for example in case of certificates.
     * The EnvVar list is then updated with this value.
     *
     * @param envVarList        current list of EnvVariables.
     * @param envVariableName   name of the EnvVar.
     * @param secretName        name of the Secret from which we will take the value.
     * @param secretFieldName   name of the field which contains the value.
     */
    public static void configureEnvVariableWithValueFromSecretOrSkip(
        List<EnvVar> envVarList,
        String envVariableName,
        String secretName,
        String secretFieldName
    ) {
        if (secretName != null && !secretName.isEmpty() && secretFieldName != null && !secretFieldName.isEmpty()) {
            envVarList.add(new EnvVarBuilder()
                .withName(envVariableName)
                .withNewValueFrom()
                    .withNewSecretKeyRef()
                        .withName(secretName)
                        .withKey(secretFieldName)
                    .endSecretKeyRef()
                .endValueFrom()
                .build()
            );
        }
    }
}
