/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

import io.fabric8.kubernetes.api.model.EnvVar;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnvironmentTest {

    @Test
    void testConfigureEnvVariableOrSkip() {
        List<EnvVar> envVars = new ArrayList<>();
        String envVariable = "MY_ENV";
        String value = "my-value";

        Environment.configureEnvVariableOrSkip(envVars, envVariable, null);
        assertThat(envVars, is(Collections.emptyList()));

        Environment.configureEnvVariableOrSkip(envVars, envVariable, value);
        assertThat(envVars.size(), is(1));
        assertThat(envVars.get(0).getName(), is(envVariable));
        assertThat(envVars.get(0).getValue(), is(value));
    }
}
