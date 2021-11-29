/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.strimzi.test.tracing.HttpContext;

/**
 * Represents information about a message that will be send
 */
public class ProducerRecord {

    private final String message;
    private final HttpContext context;

    public ProducerRecord(String message, HttpContext context) {
        this.message = message;
        this.context = context;
    }

    /**
     * @return message that will be send
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return HttpContext that will build the http request
     */
    public HttpContext getContext() {
        return context;
    }
}