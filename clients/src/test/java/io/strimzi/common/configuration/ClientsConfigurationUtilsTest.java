/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClientsConfigurationUtilsTest {

    @Test
    void testParseIntFromValue() {
        String value = "100";
        int desiredValue = 100;
        int defaultValue = 5;

        int result = ClientsConfigurationUtils.parseIntOrDefault(value, defaultValue);

        assertThat(result, is(desiredValue));

        // check when value will not be int
        value = null;

        result = ClientsConfigurationUtils.parseIntOrDefault(value, defaultValue);

        assertThat(result, is(defaultValue));

        // check when value will not be int
        value = "alice";

        result = ClientsConfigurationUtils.parseIntOrDefault(value, defaultValue);

        assertThat(result, is(defaultValue));
    }

    @Test
    void testParseLongFromValue() {
        String value = "30000";
        long desiredValue = 30000;
        long defaultValue = 5;

        long result = ClientsConfigurationUtils.parseLongOrDefault(value, defaultValue);

        assertThat(result, is(desiredValue));

        // check when value will be empty
        value = null;

        result = ClientsConfigurationUtils.parseLongOrDefault(value, defaultValue);

        assertThat(result, is(defaultValue));

        // check when value will not be long
        value = "alice";

        result = ClientsConfigurationUtils.parseLongOrDefault(value, defaultValue);

        assertThat(result, is(defaultValue));
    }

    @Test
    void testParseStringFromValue() {
        String value = "my-value";
        String defaultValue = "default";

        String result = ClientsConfigurationUtils.parseStringOrDefault(value, defaultValue);

        assertThat(result, is(value));

        // check when value will be empty
        value = null;

        result = ClientsConfigurationUtils.parseStringOrDefault(value, defaultValue);

        assertThat(result, is(defaultValue));
    }
}
