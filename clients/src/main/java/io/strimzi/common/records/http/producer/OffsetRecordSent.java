/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.records.http.producer;

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

        OffsetRecordSent ob = (OffsetRecordSent) o;
        return ob.partition == partition &&
            ob.offset == offset;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "OffsetRecordSent: " +
            "partition = " + this.partition +
            ", offset = " + this.offset;
    }
}
