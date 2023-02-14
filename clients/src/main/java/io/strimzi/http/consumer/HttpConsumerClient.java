/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.consumer;

import io.strimzi.common.ClientsInterface;

import java.util.concurrent.ScheduledFuture;

public class HttpConsumerClient implements ClientsInterface {

    @Override
    public void run() throws Exception {

    }

    @Override
    public void checkForCompletion(ScheduledFuture<?> future) {

    }
}
