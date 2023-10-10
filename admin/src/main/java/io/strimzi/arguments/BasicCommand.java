/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments;

import picocli.CommandLine;

/**
 * Base for all commands - includes basic options for specifying bootstrap server of Kafka and
 * also `help` flag
 */
public class BasicCommand implements CommandInterface {

    @CommandLine.Option(names = "--bootstrap-server", description = "Bootstrap server address", required = true)
    protected String bootstrapServer;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() {
        throw new UnsupportedOperationException("Not supported");
    }
}
