/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi;

import io.strimzi.http.producer.HttpProducerClient;
import io.strimzi.test.tracing.TracingUtil;

public class Main {
    public static void main(String[] args) throws Exception {
        TracingUtil.initialize();
        HttpProducerClient producerClient = new HttpProducerClient(System.getenv());
        producerClient.run();
    }
}