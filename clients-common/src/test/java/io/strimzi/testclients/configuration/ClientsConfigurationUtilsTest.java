/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void testParseMapOfProperties() {
        String properties = "my-key=my-value\nsecond-key=second-value";

        Properties expectedProperties = new Properties();
        expectedProperties.put("my-key", "my-value");
        expectedProperties.put("second-key", "second-value");

        Properties result = ClientsConfigurationUtils.parseMapOfProperties(properties);
        assertThat(expectedProperties, is(result));
    }

    @Test
    void testParseInvalidMapOfProperties() {
        String invalidProperties = "my-key_my-value";

        assertThrows(RuntimeException.class, () -> ClientsConfigurationUtils.parseMapOfProperties(invalidProperties));
    }

    @Test
    void testParseHeadersFromConfiguration() {
        String headers = "header_key_one=header_value_one, header_key_two=header_value_two";
        List<Header> expectedList = new ArrayList<>();
        expectedList.add(new RecordHeader("header_key_one", "header_value_one".getBytes()));
        expectedList.add(new RecordHeader("header_key_two", "header_value_two".getBytes()));

        List<Header> result = ClientsConfigurationUtils.parseHeadersFromConfiguration(headers);
        assertThat(result, is(expectedList));
    }

    @Test
    void testParseInvalidHeadersFromConfiguration() {
        String headers = null;

        List<Header> result = ClientsConfigurationUtils.parseHeadersFromConfiguration(headers);
        assertThat(result, nullValue());

        String anotherHeaders = "header_key_header_value";

        assertThrows(RuntimeException.class, () -> ClientsConfigurationUtils.parseHeadersFromConfiguration(anotherHeaders));
    }
}
