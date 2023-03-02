/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import io.strimzi.common.configuration.Constants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpConsumerConfigurationTest {

    private final Map<String, String> defaultConfiguration = Map.of(
        Constants.HOSTNAME_ENV, "localhost",
        Constants.PORT_ENV, "8080",
        Constants.TOPIC_ENV, "my-topic"
    );

    @Test
    void testDefaultConfiguration() {
        HttpConsumerConfiguration consumerConfiguration = new HttpConsumerConfiguration(defaultConfiguration);

        String baseUri = "http://localhost:8080/consumers/" + Constants.DEFAULT_GROUP_ID;
        String subscriptionUri = baseUri + "/instances/" + Constants.DEFAULT_CLIENT_ID + "/subscription";
        String consumeUri = baseUri + "/instances/" + Constants.DEFAULT_CLIENT_ID + "/records?timeout=" + Constants.DEFAULT_POLL_TIMEOUT;

        assertThat(consumerConfiguration.getClientId(), is(Constants.DEFAULT_CLIENT_ID));
        assertThat(consumerConfiguration.getGroupId(), is(Constants.DEFAULT_GROUP_ID));
        assertThat(consumerConfiguration.getPollInterval(), is(Constants.DEFAULT_POLL_INTERVAL));
        assertThat(consumerConfiguration.getPollTimeout(), is(Constants.DEFAULT_POLL_TIMEOUT));
        assertThat(consumerConfiguration.getConsumerCreationURI(), is(baseUri));
        assertThat(consumerConfiguration.getSubscriptionURI(), is(subscriptionUri));
        assertThat(consumerConfiguration.getConsumeMessagesURI(), is(consumeUri));
    }

    @Test
    void testCustomConfiguration() {
        String clientId = "arnost-client";
        String groupId = "big-group";
        long pollInterval = 66000;
        long pollTimeout = 500000;

        Map<String, String> configuration = new HashMap<>(defaultConfiguration);
        configuration.put(Constants.CLIENT_ID_ENV, clientId);
        configuration.put(Constants.GROUP_ID_ENV, groupId);
        configuration.put(Constants.POLL_INTERVAL_ENV, String.valueOf(pollInterval));
        configuration.put(Constants.POLL_TIMEOUT_ENV, String.valueOf(pollTimeout));

        HttpConsumerConfiguration consumerConfiguration = new HttpConsumerConfiguration(configuration);

        String baseUri = "http://localhost:8080/consumers/" + groupId;
        String subscriptionUri = baseUri + "/instances/" + clientId + "/subscription";
        String consumeUri = baseUri + "/instances/" + clientId + "/records?timeout=" + pollTimeout;

        assertThat(consumerConfiguration.getClientId(), is(clientId));
        assertThat(consumerConfiguration.getGroupId(), is(groupId));
        assertThat(consumerConfiguration.getPollInterval(), is(pollInterval));
        assertThat(consumerConfiguration.getPollTimeout(), is(pollTimeout));
        assertThat(consumerConfiguration.getConsumerCreationURI(), is(baseUri));
        assertThat(consumerConfiguration.getSubscriptionURI(), is(subscriptionUri));
        assertThat(consumerConfiguration.getConsumeMessagesURI(), is(consumeUri));
    }
}
