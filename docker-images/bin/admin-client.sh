#!/bin/bash

export CLASSPATH="/admin.jar:${CLASSPATH}"

java -cp $CLASSPATH io.strimzi.Main "$@"