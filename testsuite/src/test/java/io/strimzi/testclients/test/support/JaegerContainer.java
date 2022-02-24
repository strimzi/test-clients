/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.util.Collections;
import java.util.Set;

public class JaegerContainer extends GenericContainer<JaegerContainer> {

    public static final int JAEGER_QUERY_PORT = 16686;
    public static final int JAEGER_COLLECTOR_THRIFT_PORT = 14268;
    public static final int JAEGER_COLLECTOR_GRPC_PORT = 14250;
    public static final int JAEGER_ADMIN_PORT = 14269;
    public static final int JAEGER_UDP_PORT = 6831;
    public static final int ZIPKIN_PORT = 9411;

    public JaegerContainer() {
        super(ContainerUtil.getImageName(
            "JAEGER_IMAGE",
            "jaegertracing/all-in-one:1.30")
        );
        waitingFor(new BoundPortHttpWaitStrategy(JAEGER_ADMIN_PORT));
        withEnv("COLLECTOR_ZIPKIN_HTTP_PORT", String.valueOf(ZIPKIN_PORT));
        withExposedPorts(
            JAEGER_ADMIN_PORT,
            JAEGER_COLLECTOR_THRIFT_PORT,
            JAEGER_COLLECTOR_GRPC_PORT,
            JAEGER_QUERY_PORT,
            JAEGER_UDP_PORT,
            ZIPKIN_PORT
        );
        addFixedExposedPort(JAEGER_UDP_PORT, JAEGER_UDP_PORT);
        addFixedExposedPort(JAEGER_COLLECTOR_GRPC_PORT, JAEGER_COLLECTOR_GRPC_PORT);
        addFixedExposedPort(JAEGER_COLLECTOR_THRIFT_PORT, JAEGER_COLLECTOR_THRIFT_PORT);
    }

    public static class BoundPortHttpWaitStrategy extends HttpWaitStrategy {
        private final int port;

        public BoundPortHttpWaitStrategy(int port) {
            this.port = port;
        }

        @Override
        protected Set<Integer> getLivenessCheckPorts() {
            Integer mappedPort = this.waitStrategyTarget.getMappedPort(port);
            return Collections.singleton(mappedPort);
        }
    }
}
