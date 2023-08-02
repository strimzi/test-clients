package io.strimzi.common.admin.arguments;

import com.beust.jcommander.Parameter;

public class CreateCommand extends BasicCommand {
    @Parameter(names = "--count", description = "Count of topics that should be created. Default 1")
    private Integer topicsCount;

    @Parameter(names = "--partitions", description = "Number of partitions of topic(s)")
    private Integer topicPartitions;

    @Parameter(names = "--replication-factor", description = "Replication factor of topic(s)")
    private Integer topicRepFactor;
}
