#!/usr/bin/env bash

set -e

export DOCKER_ORG=${DOCKER_ORG:-strimzi-test-clients}
export DOCKER_REGISTRY=${DOCKER_REGISTRY:-quay.io}

echo "Build reason: ${BUILD_REASON}"
echo "Source branch: ${BRANCH}"

if [ "$BRANCH" == "refs/heads/main" ]; then
  export DOCKER_TAG="latest"
elif [[ "$BRANCH" == "refs/tags/"* ]]; then
  export DOCKER_TAG="${BRANCH#refs/tags/}"
else
  export DOCKER_TAG=$COMMIT
fi

./docker-images/build-images.sh build

if [ "$BUILD_REASON" == "PullRequest" ] ; then
  echo "Building Pull Request - nothing to push"
elif [[ "$BRANCH" != "refs/tags/"* ]] && [ "$BRANCH" != "refs/heads/main" ]; then
  echo "Not in main branch or in release tag - nothing to push"
else
  echo "In main branch or in release tag - pushing images"
  docker login -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_REGISTRY

  docker buildx create --use
  ./docker-images/build-images.sh
fi
