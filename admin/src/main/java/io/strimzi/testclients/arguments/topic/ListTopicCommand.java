/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.topic;

import io.strimzi.testclients.admin.AdminProperties;
import io.strimzi.testclients.arguments.BasicCommand;
import io.strimzi.testclients.constants.Constants;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

import java.util.concurrent.TimeUnit;

/**
 * Command for listing topic(s) in Kafka.
 * Accessed using `admin-client topic list`
 */
@CommandLine.Command(name = "list")
public class ListTopicCommand extends BasicCommand {

    @Override
    public Integer call() {
        return listTopics();
    }

    private Integer listTopics() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            admin.listTopics().names().get(Constants.CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS).forEach(System.out::println);
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to list topics due: " + e);
        }
    }
}
