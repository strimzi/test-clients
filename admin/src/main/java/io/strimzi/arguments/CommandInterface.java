/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments;

import java.util.concurrent.Callable;

public interface CommandInterface extends Callable<Integer> {

    @Override
    Integer call();
}
