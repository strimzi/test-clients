/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.http.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerRecord {
    private String topic;
    private Object key;
    private Object value;
    private int partition;
    private long offset;

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
