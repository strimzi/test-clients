/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.Transferable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Supplier;

public class KafkaBridgeContainer extends GenericContainer<KafkaBridgeContainer> {
    private static final int EMBEDDED_HTTP_SERVER_PORT = 8080;
    private static final String CONFIG_PATH = "/opt/strimzi/config/application2.properties";

    private final Supplier<String> bootstrapServers;
    private String tracing;

    public KafkaBridgeContainer(Supplier<String> bootstrapServers) {
        super(ContainerUtil.getImageName(
            "KAFKA_BRIDGE_IMAGE",
            "quay.io/strimzi/kafka-bridge:0.21.4")
        );
        this.bootstrapServers = bootstrapServers;
        setExposedPorts(Collections.singletonList(EMBEDDED_HTTP_SERVER_PORT));
        setNetwork(Network.SHARED);
        setCommandParts(new String[]{
            "/opt/strimzi/bin/kafka_bridge_run.sh",
            "--config-file=" + CONFIG_PATH
        });
        withLogConsumer(new Slf4jLogConsumer(logger()));
    }

    public KafkaBridgeContainer withTracing(String tracing) {
        this.tracing = tracing;
        return self();
    }

    public int getEmbeddedHttpServerPort() {
        return getMappedPort(EMBEDDED_HTTP_SERVER_PORT);
    }

    @Override
    protected void containerIsCreated(String containerId) {
        super.containerIsCreated(containerId);
        try {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                Properties properties = new Properties();
                properties.load(is);
                String kbs = bootstrapServers.get();
                properties.put("kafka.bootstrap.servers", kbs);
                if (tracing != null) {
                    properties.put("bridge.tracing", tracing);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                properties.store(baos, "Test");
                logger().info("Overriding Kafka bootstrap servers: {}", kbs);
                copyFileToContainer(
                    Transferable.of(baos.toByteArray(), 0777),
                    CONFIG_PATH
                );
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
