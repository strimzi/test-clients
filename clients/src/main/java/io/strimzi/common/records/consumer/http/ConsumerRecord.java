/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.records.consumer.http;

import java.util.Objects;

public class ConsumerRecord {
    private String topic;
    private Object key;
    private Object value;
    private int partition;
    private long offset;
    private Long timestamp;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;

        // null check
        if (o == null)
            return false;

        // type check and cast
        if (getClass() != o.getClass())
            return false;

        ConsumerRecord cr = (ConsumerRecord) o;

        return Objects.equals(cr.timestamp, timestamp) &&
            cr.partition == partition &&
            cr.offset == offset &&
            Objects.equals(cr.value, value) &&
            Objects.equals(cr.topic, topic) &&
            Objects.equals(cr.key, key);
    }

    @Override
    public int hashCode() {
        return topic.hashCode() + key.hashCode();
    }

    @Override
    public String toString() {
        return "ConsumerRecord: " +
            ", topic = " + this.topic +
            ", key = " + this.key +
            ", value = " + this.value +
            ", partition = " + this.partition +
            ", offset = " + this.offset +
            ", timestamp = " + this.timestamp;
    }
}
