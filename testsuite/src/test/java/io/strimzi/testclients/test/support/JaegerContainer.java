/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.support;

import org.testcontainers.containers.GenericContainer;

public class JaegerContainer extends GenericContainer<JaegerContainer> {
    public JaegerContainer() {
        super(ContainerUtil.getImageName(
            "JAEGER_IMAGE",
            "jaegertracing/all-in-one:1.30")
        );
    }
}
