#!/usr/bin/env bash

KAFKA_VERSIONS=$(cat docker-images/kafka.version)
KAFKA_MODULES=$(ls -d kafka/*/)
HTTP_MODULES=$(ls -d http/*/)
MODE=${1:-"all"}
DOCKER_TAG=${DOCKER_TAG:-"latest"}

echo "Building Kafka clients with versions inside kafka.version file"
echo $MODE
for KAFKA_VERSION in $KAFKA_VERSIONS
do
  for KAFKA_MODULE in $KAFKA_MODULES
  do
    if [ $MODE = "build" ];
      then
        MVN_ARGS="-Dkafka.version=$KAFKA_VERSION" make build --directory=$KAFKA_MODULE
      else
        DOCKER_TAG="$DOCKER_TAG-$KAFKA_VERSION" MVN_ARGS="-Dkafka.version=$KAFKA_VERSION" make all --directory=$KAFKA_MODULE
    fi
  done
done

for HTTP_MODULE in $HTTP_MODULES
do
  if [ $MODE = "build" ];
  then
    make build --directory=$HTTP_MODULE
  else
    make all --directory=$HTTP_MODULE
  fi
done