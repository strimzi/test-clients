/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments.topic;

import io.strimzi.admin.AdminProperties;
import io.strimzi.models.KafkaTopicDescription;
import io.strimzi.utils.DescribeTopicsUtils;
import io.strimzi.utils.OutputFormat;
import org.apache.kafka.clients.admin.Admin;
import picocli.CommandLine;

import java.util.LinkedList;
import java.util.List;

/**
 * Command for describing topic(s).
 * Describes topic(s) based on topics count and topic name or prefix
 * Accessed using `admin-client topic describe`
 */
@CommandLine.Command(name = "describe")
public class DescribeTopicCommand extends BasicTopicCommand {

    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "plain", description = "Output format supports: ${COMPLETION-CANDIDATES}")
    private OutputFormat outputFormat;

    @Override
    public Integer call() {
        return describeTopics();
    }

    /**
     * Describes topics that are in Kafka using Kafka Admin client and list of topic names from {@link BasicTopicCommand#getListOfTopicNames()}
     * @return return code of operation, in case of exception `1` is returned
     */
    private Integer describeTopics() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            List<KafkaTopicDescription> kafkaTopicDescriptionList = new LinkedList<>();

            // parse response from java admin
            admin.describeTopics(this.getListOfTopicNames()).topicNameValues().forEach((key, value) -> {
                try {
                    kafkaTopicDescriptionList.add(KafkaTopicDescription.parseKafkaTopicDescription(value.get()));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to describe topic: " + key + " due: " + e.getCause());
                }
            });

            // provide response
            final String cmdOutput = DescribeTopicsUtils.getOutput(outputFormat, kafkaTopicDescriptionList);
            System.out.println(cmdOutput);

        } catch (Exception e) {
            throw new RuntimeException("Unable to describe topics due: " + e);
        }
        return 0;
    }
}
