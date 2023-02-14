/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.http.producer;

import io.strimzi.test.tracing.HttpContext;

/**
 * Represents information about a message that will be send
 */
public record ProducerRecord(String message, HttpContext context) {}
