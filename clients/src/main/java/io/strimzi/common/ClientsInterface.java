/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

public interface ClientsInterface {
    void run();
    void awaitCompletion();
    void checkFinalState();
}
