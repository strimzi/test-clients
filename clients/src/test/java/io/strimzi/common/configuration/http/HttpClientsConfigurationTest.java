/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common.configuration.http;

import io.strimzi.common.configuration.Constants;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

public class HttpClientsConfigurationTest {

    @Test
    void testEmptyAndDefaultConfiguration() {
        Map<String, String> configuration = new HashMap<>();

        String hostname = "localhost";
        String port = "8080";
        String topic = "my-topic";

        // leaving empty configuration will throw exception - hostname, port and topic are not set
        assertThrows(InvalidParameterException.class, () -> new HttpClientsConfiguration(configuration));

        configuration.put(Constants.HOSTNAME_ENV, hostname);

        // exception will be thrown - port and topic are not set
        assertThrows(InvalidParameterException.class, () -> new HttpClientsConfiguration(configuration));

        configuration.put(Constants.PORT_ENV, port);

        // exception will be thrown - topic is not set
        assertThrows(InvalidParameterException.class, () -> new HttpClientsConfiguration(configuration));

        configuration.put(Constants.TOPIC_ENV, topic);

        // exception will not be thrown
        HttpClientsConfiguration clientsConfiguration = new HttpClientsConfiguration(configuration);

        assertThat(clientsConfiguration.getDelay(), is(Constants.DEFAULT_DELAY_MS));
        assertThat(clientsConfiguration.getHostname(), is(hostname));
        assertThat(clientsConfiguration.getMessageCount(), is(Constants.DEFAULT_MESSAGES_COUNT));
        assertThat(clientsConfiguration.getPort(), is(port));
        assertThat(clientsConfiguration.getTopic(), is(topic));
        assertThat(clientsConfiguration.getEndpointPrefix(), is(Constants.DEFAULT_ENDPOINT_PREFIX));
    }

    @Test
    void testCustomConfiguration() {
        Map<String, String> configuration = new HashMap<>();

        String hostname = "my-hostname";
        String port = "9999";
        String topic = "my-topic";
        long delayMs = 30000;
        int messageCount = 333;
        String endpointPrefix = "/prefix";

        configuration.put(Constants.HOSTNAME_ENV, hostname);
        configuration.put(Constants.PORT_ENV, port);
        configuration.put(Constants.TOPIC_ENV, topic);
        configuration.put(Constants.DELAY_MS_ENV, String.valueOf(delayMs));
        configuration.put(Constants.MESSAGE_COUNT_ENV, String.valueOf(messageCount));
        configuration.put(Constants.ENDPOINT_PREFIX_ENV, endpointPrefix);

        HttpClientsConfiguration clientsConfiguration = new HttpClientsConfiguration(configuration);

        assertThat(clientsConfiguration.getDelay(), is(delayMs));
        assertThat(clientsConfiguration.getEndpointPrefix(), is(endpointPrefix));
        assertThat(clientsConfiguration.getMessageCount(), is(messageCount));
        assertThat(clientsConfiguration.getPort(), is(port));
        assertThat(clientsConfiguration.getTopic(), is(topic));
        assertThat(clientsConfiguration.getHostname(), is(hostname));
    }

    @Test
    void testInvalidConfiguration() {
        Map<String, String> configuration = new HashMap<>();

        String hostname = "my-hostname";
        int port = 9999;
        String topic = "my-topic";
        String delayMs = "arnost";
        String messageCount = "alice";

        configuration.put(Constants.HOSTNAME_ENV, hostname);
        configuration.put(Constants.PORT_ENV, String.valueOf(port));
        configuration.put(Constants.TOPIC_ENV, topic);
        configuration.put(Constants.DELAY_MS_ENV, delayMs);
        configuration.put(Constants.MESSAGE_COUNT_ENV, messageCount);

        HttpClientsConfiguration clientsConfiguration = new HttpClientsConfiguration(configuration);

        assertThat(clientsConfiguration.getDelay(), is(not(delayMs)));
        assertThat(clientsConfiguration.getDelay(), is(Constants.DEFAULT_DELAY_MS));

        assertThat(clientsConfiguration.getMessageCount(), is(not(messageCount)));
        assertThat(clientsConfiguration.getMessageCount(), is(Constants.DEFAULT_MESSAGES_COUNT));
    }
}
