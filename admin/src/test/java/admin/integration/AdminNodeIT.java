/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package admin.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdminNodeIT extends AbstractIT {
    @Test
    void testDescribeNodes() throws JsonProcessingException {
        // firstly describe all nodes
        cmd.execute("node", "describe", "--node-ids", "all", "-o", "json");
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode nodesArray = objectMapper.readTree(OUT.toString());
        JsonNode nodes = nodesArray.get("nodes");

        assertThat(nodes.size(), is(3));

        OUT.reset();
        // then describe nodes 0 and 1
        cmd.execute("node", "describe", "--node-ids", "0,1", "-o", "json");
        nodesArray = objectMapper.readTree(OUT.toString());
        nodes = nodesArray.get("nodes");

        assertThat(nodes.size(), is(2));

        List<String> nodeIds = new ArrayList<>();

        for (JsonNode node : nodes) {
            nodeIds.add(node.get("id").asText());
        }

        assertThat(nodeIds.containsAll(List.of("0", "1")), is(true));

        OUT.reset();

        // lastly describe just node 2
        cmd.execute("node", "describe", "--node-ids", "2", "-o", "json");

        nodesArray = objectMapper.readTree(OUT.toString());
        nodes = nodesArray.get("nodes");

        assertThat(nodes.size(), is(1));
        assertThat(nodes.get(0).get("id").asText(), is("2"));
    }
}
