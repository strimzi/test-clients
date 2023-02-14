/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

import java.util.function.Function;

public class ClientsConfigurationUtils {
    public static long parseLongOrDefault(String value, long defaultValue) {
        return parseOrDefault(value, Long::parseLong, defaultValue);
    }

    public static int parseIntOrDefault(String value, int defaultValue) {
        return parseOrDefault(value, Integer::parseInt, defaultValue);
    }

    public static String parseStringOrDefault(String value, String defaultValue) {
        return parseOrDefault(value, String::toString, defaultValue);
    }

    private static <T> T parseOrDefault(String value, Function<String, T> converter, T defaultValue) {
        T returnValue = defaultValue;

        if (value != null) {
            returnValue = converter.apply(value);
        }
        return returnValue;
    }
}
