/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.net.http.HttpRequest;

/**
 * Represents information about a message that will be send
 */
public class ProducerRecord {

    private final String message;
    private final HttpRequest request;

    public ProducerRecord(String message, HttpRequest request) {
        this.message = message;
        this.request = request;
    }

    /**
     * @return message that will be send
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return HttpRequest that will be executed
     */
    public HttpRequest getRequest() {
        return request;
    }
}