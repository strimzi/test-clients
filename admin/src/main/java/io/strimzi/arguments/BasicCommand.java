/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.arguments;

import picocli.CommandLine;

import java.util.concurrent.Callable;

public class BasicCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--bootstrap-server", description = "Bootstrap server address", required = true)
    protected String bootstrapServer;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() {
        return null;
    }
}
