/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.common;

import java.util.Arrays;
import java.util.List;

public enum MessageType {
    TEXT("text"),
    JSON("json"),
    UNKNOWN("unknown");

    private final String messageType;

    MessageType(String messageType) {
        this.messageType = messageType;
    }

    public static MessageType getFromString(String value) {
        for (MessageType type : values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return this.messageType;
    }

    public static List<MessageType> supportedTypes() {
        return Arrays.stream(values()).filter(item -> item != UNKNOWN).toList();
    }
}
