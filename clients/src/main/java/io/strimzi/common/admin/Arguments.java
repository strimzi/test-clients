package io.strimzi.common.admin;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = "--operation", description = "Operation that will be done with the resource(s) - list, create, delete, describe, update", required = true)
    private String operation;


}
