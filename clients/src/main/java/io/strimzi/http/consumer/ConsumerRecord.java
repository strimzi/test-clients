/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.consumer;

public record ConsumerRecord(String topic, Object key, Object value, int partition, long offset) {
    @Override
    public String toString() {
        return "ConsumerRecord: " +
            ", topic = " + this.topic +
            ", key = " + this.key +
            ", value = " + this.value +
            ", partition = " + this.partition +
            ", offset = " + this.offset;
    }
}
