#!/bin/bash

export CLASSPATH="/admin.jar:${CLASSPATH}"

java -cp $CLASSPATH io.strimzi.testclients.Main "$@"