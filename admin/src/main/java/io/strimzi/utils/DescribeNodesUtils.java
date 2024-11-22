/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.Node;

import java.util.List;

public class DescribeNodesUtils {
    /**
     * Returns the output of the `node describe` command based on the `--output` option.
     * Possible formats are `plain` (normal text) and `json`.
     *
     * @param outputFormat  format we should return the nodes description in
     * @param nodes         list of nodes
     *
     * @return  output of the `node describe` command in specified format
     * @throws JsonProcessingException  during parsing the JSON
     */
    public static String getOutput(OutputFormat outputFormat, List<Node> nodes) throws JsonProcessingException {
        return switch (outputFormat) {
            case PLAIN -> getPlainOutput(nodes);
            case JSON -> getJsonOutput(nodes);
        };
    }

    /**
     * Returns the description of the nodes in plain format.
     * @param nodes     list of nodes that should have output in plain format
     * @return the description of the nodes in plain format.
     */
    private static String getPlainOutput(List<Node> nodes) {
        StringBuilder stringBuilder = new StringBuilder();

        // build the plain output
        nodes.forEach(node -> {
            String nodeDescription = String.format(
                "Node %s" +
                "\n  hostname: %s:%s" +
                "\n  node ID: %s" +
                "\n  rack: %s\n",
                node.idString(), node.host(), node.port(), node.idString(), node.rack()
            );

            stringBuilder.append(nodeDescription);
        });

        return stringBuilder.toString();
    }

    /**
     * Returns the description of the nodes in JSON format.
     * @param nodes     list of nodes that should have output in JSON format
     * @return  the description of the nodes in JSON format.
     * @throws JsonProcessingException  in case of exception during parsing the JSON object
     */
    private static String getJsonOutput(List<Node> nodes) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        ArrayNode nodesArray = mapper.createArrayNode();

        nodes.forEach(node -> {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("id", node.idString());
            objectNode.put("hostname", String.format("%s:%s", node.host(), node.id()));
            objectNode.put("rack", node.rack());

            nodesArray.add(objectNode);
        });

        root.set("nodes", nodesArray);

        return mapper.writeValueAsString(root);
    }
}
