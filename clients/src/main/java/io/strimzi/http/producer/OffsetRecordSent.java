/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.producer;

public class OffsetRecordSent {
    private int partition;
    private long offset;

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "OffsetRecordSent: " +
            "partition = " + this.partition +
            ", offset = " + this.offset;
    }
}
