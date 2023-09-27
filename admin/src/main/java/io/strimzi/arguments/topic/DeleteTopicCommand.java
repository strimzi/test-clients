/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

@CommandLine.Command(name = "delete")
public class DeleteTopicCommand extends BasicTopicCommand {
    @Override
    public Integer call() {
        return deleteTopic();
    }

    private Integer deleteTopic() {
        Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer));

        try {
            admin.deleteTopics(this.getListOfTopicNames()).all().get();
            System.out.println("Topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " successfully deleted");
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete topic(s) with name/prefix: " + this.getTopicPrefixOrName() + " due: " + e.getCause());
        }
    }
}
