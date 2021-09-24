#!/usr/bin/env bash

set -e

export DOCKER_ORG=${DOCKER_ORG:-strimzi-test-clients}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-quay.io}
export DOCKER_TAG=$COMMIT

echo "Build reason: ${BUILD_REASON}"
echo "Source branch: ${BRANCH}"

./docker-images/build-images.sh build

if [ "$BUILD_REASON" == "PullRequest" ] ; then
  echo "Building Pull Request - nothing to push"
elif [[ "$BRANCH" != "refs/tags/"* ]] && [ "$BRANCH" != "refs/heads/main" ]; then
  echo "Not in main branch or in release tag - nothing to push"
else
  if [ "$BRANCH" == "refs/heads/main" ]; then
      export DOCKER_TAG="latest"
  else
      export DOCKER_TAG="${BRANCH#refs/tags/}"
  fi
  echo "In main branch or in release tag - pushing images"
  docker login -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_REGISTRY

  ./docker-images/build-images.sh
fi
