/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package admin.integration;

import io.strimzi.testclients.admin.KafkaAdminClient;
import io.strimzi.test.container.StrimziKafkaCluster;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractIT {
    protected static KafkaAdminClient adminClient = new KafkaAdminClient();
    protected static CommandLine cmd = new CommandLine(adminClient);
    protected static StrimziKafkaCluster kafkaCluster;
    protected static Admin admin;

    protected static final PrintStream ORIGINAL_OUT = System.out;
    protected static final PrintStream ORIGINAL_ERR = System.err;
    protected static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();
    protected static final ByteArrayOutputStream ERR = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        OUT.reset();
        ERR.reset();
        System.setOut(new PrintStream(OUT));
        System.setErr(new PrintStream(ERR));
    }

    @BeforeAll
    static void setup() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        kafkaCluster = new StrimziKafkaCluster.StrimziKafkaClusterBuilder()
            .withNumberOfBrokers(3)
            .withInternalTopicReplicationFactor(1)
            .withSharedNetwork()
            .build();
        kafkaCluster.start();
        cmd.setOut(printWriter);
        cmd.execute("configure", "common", "--bootstrap-server", kafkaCluster.getBootstrapServers());

        admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster.getBootstrapServers()));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(ORIGINAL_OUT);
        System.setErr(ORIGINAL_ERR);
    }

    @AfterAll
    static void afterAll() {
        kafkaCluster.stop();
    }
}
