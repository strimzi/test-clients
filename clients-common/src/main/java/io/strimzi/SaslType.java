/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi;

import java.util.List;

public enum SaslType {
    PLAIN("PLAIN", "PLAIN"),
    SCRAM_SHA_256("SCRAM-SHA-256", "SCRAM-SHA-256"),
    SCRAM_SHA_512("SCRAM-SHA-512", "SCRAM-SHA-512"),
    UNKNOWN("UNKNOWN", "NONE");

    private final String name;
    private final String kafkaProperty;

    SaslType(String name, String kafkaProperty) {
        this.name = name;
        this.kafkaProperty = kafkaProperty;
    }

    public static List<String> getAllSaslTypes() {
        return List.of(PLAIN.name, SCRAM_SHA_256.name, SCRAM_SHA_512.name);
    }

    public String getKafkaProperty() {
        return this.kafkaProperty;
    }

    public String getName() {
        return name;
    }

    public static SaslType getFromString(String value) {
        for (SaslType type : values()) {
            if (type.getName().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

