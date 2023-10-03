/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin;

import org.apache.kafka.clients.CommonClientConfigs;

import java.util.Properties;

public class AdminProperties {

    public static Properties adminProperties(String bootstrapServer) {
        Properties properties = new Properties();

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        return properties;
    }
}
