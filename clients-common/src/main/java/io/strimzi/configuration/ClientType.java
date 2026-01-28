/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.configuration;

public enum ClientType {
    KafkaProducer,
    KafkaConsumer,
    KafkaAdmin,
    KafkaStreams,
    HttpProducer,
    HttpConsumer,
    Unknown;

    public static ClientType getFromString(String value) {
        for (ClientType type : values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return Unknown;
    }
}
