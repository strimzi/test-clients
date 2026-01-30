/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.arguments.nodes;

import io.strimzi.testclients.admin.AdminProperties;
import io.strimzi.testclients.arguments.BasicCommand;
import io.strimzi.testclients.constants.Constants;
import io.strimzi.testclients.utils.DescribeNodesUtils;
import io.strimzi.testclients.utils.OutputFormat;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.Node;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;

/**
 * Command for describing the node(s) of the Kafka cluster.
 * It gets the collection of the nodes using the {@link Admin#describeCluster()} method and then, based on the configuration
 * of the {@link #nodeIds} - specified by `--node-ids` option - it filters the nodes that we would like to describe.
 * In case that user specify `all` in the `--node-ids` option, all the nodes will be described.
 * Otherwise, users can specify IDs - which have to be separated by commas.
 * This is accessed using `admin-client node describe`
 */
@CommandLine.Command(name = "describe")
public class DescribeNodeCommand extends BasicCommand {
    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "plain", description = "Output format supports: ${COMPLETION-CANDIDATES}")
    OutputFormat outputFormat;

    @CommandLine.Option(names = {"--node-ids"}, description = "Comma-separated list of nodes which we want to describe. For describing all nodes, you can use `all`.", required = true)
    String nodeIds;

    @Override
    public Integer call() {
        return describeNode();
    }

    /**
     * Describes the nodes of the Kafka cluster by connecting to the specified {@link #bootstrapServer}.
     * Based on the configuration of the {@link #nodeIds}, it filters the nodes and describe them in the console.
     *
     * @return return code of the operation
     */
    private Integer describeNode() {
        try (Admin admin = Admin.create(AdminProperties.adminProperties(this.bootstrapServer))) {
            List<Node> nodes = admin.describeCluster().nodes().get().stream().toList();
            if (!nodeIds.equals(Constants.ALL_OPTION)) {
                List<String> listOfNodeIds = Arrays.stream(nodeIds.split(",")).toList();
                nodes = nodes.stream().filter(node -> listOfNodeIds.contains(node.idString())).toList();
            }

            System.out.println(DescribeNodesUtils.getOutput(outputFormat, nodes));
            return 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to list cluster nodes due to: " + e.getCause());
        }
    }
}
