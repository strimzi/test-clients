/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.unit.configuration.http;

import io.strimzi.testclients.configuration.ConfigurationConstants;
import io.strimzi.testclients.configuration.http.HttpProducerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpProducerConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        Map<String, String> defaultConfiguration = Map.of(
            ConfigurationConstants.HOSTNAME_ENV, "localhost",
            ConfigurationConstants.PORT_ENV, "8080",
            ConfigurationConstants.TOPIC_ENV, "my-topic"
        );

        String expectedUri = "http://localhost:8080/topics/my-topic";
        HttpProducerConfiguration producerConfiguration = new HttpProducerConfiguration(defaultConfiguration);

        assertThat(producerConfiguration.getMessage(), is(ConfigurationConstants.DEFAULT_MESSAGE));
        assertThat(producerConfiguration.getUri(), is(expectedUri));
    }

    @Test
    void testCustomConfiguration() {
        String hostname = "my-hostname";
        String port = "9999";
        String topic = "custom-topic";
        String endpointPrefix = "/prefix";
        String expectedUri = "http://" + hostname + ":" + port + endpointPrefix + "/topics/" + topic;
        String message = "Custom message";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.MESSAGE_ENV, message);
        configuration.put(ConfigurationConstants.HOSTNAME_ENV, hostname);
        configuration.put(ConfigurationConstants.PORT_ENV, port);
        configuration.put(ConfigurationConstants.TOPIC_ENV, topic);
        configuration.put(ConfigurationConstants.ENDPOINT_PREFIX_ENV, endpointPrefix);

        HttpProducerConfiguration producerConfiguration = new HttpProducerConfiguration(configuration);

        assertThat(producerConfiguration.getMessage(), is(message));
        assertThat(producerConfiguration.getUri(), is(expectedUri));
    }

    @Test
    void testSslConfiguration() {
        String hostname = "my-hostname";
        String port = "8443";
        String topic = "custom-topic";
        String endpointPrefix = "/prefix";
        String expectedUri = "https://" + hostname + ":" + port + endpointPrefix + "/topics/" + topic;
        String cert = "my-cert";

        Map<String, String> configuration = new HashMap<>();
        configuration.put(ConfigurationConstants.HOSTNAME_ENV, hostname);
        configuration.put(ConfigurationConstants.PORT_ENV, port);
        configuration.put(ConfigurationConstants.TOPIC_ENV, topic);
        configuration.put(ConfigurationConstants.ENDPOINT_PREFIX_ENV, endpointPrefix);
        configuration.put(ConfigurationConstants.CA_CRT_ENV, cert);

        HttpProducerConfiguration producerConfiguration = new HttpProducerConfiguration(configuration);

        assertThat(producerConfiguration.getUri(), is(expectedUri));
    }
}
