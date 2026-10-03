/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.testutils;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Helpers shared by the integration tests of the clients and admin modules.
 */
public class TestUtils {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(200);

    /**
     * Polls {@code condition} until it returns true, failing after {@link #DEFAULT_TIMEOUT}.
     *
     * @param description   what is being waited for, used in the failure message
     * @param condition     condition to poll; an exception counts as "not yet" (e.g. metadata not propagated)
     */
    public static void waitFor(String description, Callable<Boolean> condition) {
        waitFor(description, DEFAULT_POLL_INTERVAL, DEFAULT_TIMEOUT, condition);
    }

    /**
     * Polls {@code condition} until it returns true, failing after {@code timeout}.
     *
     * @param description   what is being waited for, used in the failure message
     * @param pollInterval  pause between two checks
     * @param timeout       how long to wait before failing
     * @param condition     condition to poll; an exception counts as "not yet" and is attached to the failure
     */
    public static void waitFor(String description, Duration pollInterval, Duration timeout, Callable<Boolean> condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        Exception lastException = null;

        while (true) {
            try {
                if (Boolean.TRUE.equals(condition.call())) {
                    return;
                }
            } catch (Exception e) {
                lastException = e;
            }

            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out after " + timeout + " waiting for " + description, lastException);
            }

            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for " + description, e);
            }
        }
    }
}
