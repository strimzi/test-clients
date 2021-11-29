/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.tracing;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Tracing http handle -- to be able to finish span + get HttpRequest
 */
public class HttpHandle<T> {

    protected HttpRequest.Builder builder(HttpContext context) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(context.getUri()))
                .setHeader(context.getHeaderKey(), context.getHeaderValue());

            if (context.getRecord() == null) {
                builder.GET();
            } else {
                builder.POST(HttpRequest.BodyPublishers.ofString(context.getRecord()));
            }

            return builder;
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public HttpRequest build(HttpContext context) {
        return builder(context).build();
    }

    public HttpResponse<T> finish(HttpResponse<T> response) {
        return response;
    }
}