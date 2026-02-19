include ./Makefile.os
include ./Makefile.docker
include ./Makefile.maven

RELEASE_VERSION ?= latest
VERSION ?= `cat clients.version`
PROJECT_NAME = test-clients

DOCKER_TARGETS = docker_build docker_push docker_tag docker_load docker_save docker_delete_archive docker_amend_manifest docker_gha_sign_manifest docker_gha_sbom docker_gha_push_sbom

release: release_examples release_maven release_clients_version

next_version:
	mvn versions:set -DnewVersion=$(shell echo $(NEXT_VERSION) | tr a-z A-Z)
	mvn versions:commit
	echo "$(NEXT_VERSION)\c" > clients.version

release_examples:
	echo "Changing images in examples to: $(RELEASE_VERSION)"
	$(FIND) ./examples -name '*.yaml' -type f -exec $(SED) -i '/image: "\?quay.io\/strimzi-test-clients\/[a-zA-Z0-9_.-]\?\+:[a-zA-Z0-9_.-]\+-kafka-[0-9.]\+"\?/s/:[a-zA-Z0-9_.-]\+-kafka-\([0-9.]\+\)/:$(RELEASE_VERSION)-kafka-\1/g' {} \;

release_maven:
	echo "Update pom versions to: $(RELEASE_VERSION)"
	mvn versions:set -DnewVersion=$(shell echo $(RELEASE_VERSION) | tr a-z A-Z)
	mvn versions:commit

release_clients_version:
	echo "$(RELEASE_VERSION)\c" > clients.version

clean: make java_clean

pushtocentral:
	./.github/scripts/push-to-central.sh