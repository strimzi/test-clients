package io.strimzi.common.admin.arguments;

import com.beust.jcommander.Parameter;

public class BasicCommand {
    @Parameter(names = "--topic", description = "Name or prefix of the topic(s) that should be operated with", required = true)
    private String topicName;

    @Parameter(names = "--bootstrap-server", description = "Bootstrap server of Kafka", required = true)
    private String bootstrapServer;

    @Parameter(names = "--help", help = true)
    private boolean help;
}
