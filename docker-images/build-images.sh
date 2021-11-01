#!/usr/bin/env bash

KAFKA_VERSIONS=$(cat docker-images/kafka.version)
KAFKA_MODULES=$(ls -d kafka/*/ | grep -Ev ".*/target/")
HTTP_MODULES=$(ls -d http/*/ | grep -Ev ".*/target/")
MODE=${1:-"push"}
DOCKER_TAG=${DOCKER_TAG:-"latest"}
MVN_ARGS=${MVN_ARGS:-""}

echo "Installing root pom"
make java_install_single

echo "Building Tracing dependency"
make build --directory=tracing/

echo "Building Kafka clients with versions inside kafka.version file"
echo "Used build mode: $MODE"
for KAFKA_VERSION in $KAFKA_VERSIONS
do
  for KAFKA_MODULE in $KAFKA_MODULES
  do
    if [ $MODE = "build" ];
      then
        DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" MVN_ARGS="$MVN_ARGS -Dkafka.version=$KAFKA_VERSION" make build --directory=$KAFKA_MODULE
      else
        DOCKER_TAG="$DOCKER_TAG-kafka-$KAFKA_VERSION" MVN_ARGS="$MVN_ARGS-Dkafka.version=$KAFKA_VERSION" make docker_push --directory=$KAFKA_MODULE
    fi
  done
done

for HTTP_MODULE in $HTTP_MODULES
do
  if [ $MODE = "build" ];
  then
    make build --directory=$HTTP_MODULE
  else
    make docker_push --directory=$HTTP_MODULE
  fi
done