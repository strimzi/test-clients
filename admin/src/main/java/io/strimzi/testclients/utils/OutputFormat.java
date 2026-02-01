/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.utils;

/**
 * This enumeration specifies the different types of formats
 * that can be used to represent and output data.
 */
public enum OutputFormat {
    JSON("json"),
    PLAIN("plain");

    private final String format;

    OutputFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return this.format;
    }
}
