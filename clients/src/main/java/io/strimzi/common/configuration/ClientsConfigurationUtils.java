/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class ClientsConfigurationUtils {

    private static final Logger LOGGER = LogManager.getLogger(ClientsConfigurationUtils.class);

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

        try {
            if (value != null) {
                returnValue = converter.apply(value);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse value from: {}, using default", value);
            returnValue = defaultValue;
        }
        return returnValue;
    }
}
