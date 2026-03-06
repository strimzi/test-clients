/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments;

import java.util.concurrent.Callable;

/**
 * Default interface for all commands of the admin-client.
 */
public interface CommandInterface extends Callable<Integer> {

    @Override
    Integer call();
}
