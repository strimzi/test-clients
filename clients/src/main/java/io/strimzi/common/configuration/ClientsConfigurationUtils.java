/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
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

    public static Properties parseMapOfProperties(String properties) {
        Properties additionalProps = new Properties();

        if (!properties.isEmpty()) {
            StringTokenizer tok = new StringTokenizer(properties, System.lineSeparator());
            while (tok.hasMoreTokens()) {
                String record = tok.nextToken();
                int endIndex = record.indexOf('=');
                if (endIndex == -1) {
                    throw new RuntimeException("Failed to parse Map from String");
                }
                String key = record.substring(0, endIndex);
                String value = record.substring(endIndex + 1);
                additionalProps.put(key.trim(), value.trim());
            }
        }

        return additionalProps;
    }

    public static List<Header> parseHeadersFromConfiguration(String headers) {
        List<Header> headersList = null;

        if (headers != null) {
            headersList = new ArrayList<>();

            String[] headersArray = headers.split(", [\t\n\r]?");
            for (String header : headersArray) {
                headersList.add(new RecordHeader(header.split("=")[0], header.split("=")[1].getBytes()));
            }
        }

        return headersList;
    }
}
