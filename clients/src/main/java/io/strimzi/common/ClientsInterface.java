/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import java.util.concurrent.ScheduledFuture;

public interface ClientsInterface {
    void run() throws Exception;
    void checkForCompletion(ScheduledFuture<?> future);
}
