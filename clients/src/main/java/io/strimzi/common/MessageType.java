/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

public enum MessageType {
    text,
    json,
    Unknown;

    public static MessageType getFromString(String value) {
        for (MessageType type : values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return Unknown;
    }
}
