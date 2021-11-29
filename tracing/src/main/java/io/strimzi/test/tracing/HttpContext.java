/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

/**
 * Tracing http context
 */
public class HttpContext {
    private final String uri;
    private final String headerKey;
    private final String headerValue;
    private final String record;

    private HttpContext(String uri, String headerKey, String headerValue, String record) {
        this.uri = uri;
        this.headerKey = headerKey;
        this.headerValue = headerValue;
        this.record = record;
    }

    public static HttpContext post(String uri, String contentType, String record) {
        return new HttpContext(uri, "content-type", contentType, record);
    }

    public static HttpContext get(String uri, String accept) {
        return new HttpContext(uri, "accept", accept, null);
    }

    public String getUri() {
        return uri;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public String getRecord() {
        return record;
    }
}