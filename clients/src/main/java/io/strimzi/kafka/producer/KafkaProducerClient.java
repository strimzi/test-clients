/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka.producer;

import io.strimzi.common.ClientsInterface;

import java.util.concurrent.ScheduledFuture;

public class KafkaProducerClient implements ClientsInterface {

    @Override
    public void run() throws Exception {

    }

    @Override
    public void checkForCompletion(ScheduledFuture<?> future) {

    }
}
