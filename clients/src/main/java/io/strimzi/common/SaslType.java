/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import java.util.List;

public enum SaslType {
    PLAIN("PLAIN", "PLAIN"),
    SCRAM_SHA_256("SCRAM-SHA-256", "SCRAM-SHA256"),
    SCRAM_SHA_512("SCRAM-SHA-512", "SCRAM-SHA512"),
    UNKNOWN("UNKNOWN", "NONE");

    private final String name;
    private final String kafkaProperty;

    private SaslType(String name, String kafkaProperty) {
        this.name = name;
        this.kafkaProperty = kafkaProperty;
    }

    public static List<String> getAllSaslTypes() {
        return List.of(PLAIN.name, SCRAM_SHA_256.name, SCRAM_SHA_512.name);
    }

    public String getKafkaProperty() {
        return this.kafkaProperty;
    }
}

