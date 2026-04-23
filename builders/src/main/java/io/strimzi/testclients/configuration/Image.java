/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.testclients.configuration;

import io.sundr.builder.annotations.Buildable;

@Buildable(editableEnabled = false)
public class Image {
    public static final String STRIMZI_TEST_CLIENTS_DEFAULT_IMAGE_ENV = "STRIMZI_TEST_CLIENTS_DEFAULT_IMAGE";
    public static final String TEST_CLIENTS_DEFAULT_IMAGE = "quay.io/strimzi-test-clients/test-clients:latest-kafka-4.2.0";

    private String imageName = Environment.getEnvOrDefault(STRIMZI_TEST_CLIENTS_DEFAULT_IMAGE_ENV, TEST_CLIENTS_DEFAULT_IMAGE);
    private String imagePullPolicy = "IfNotPresent";
    private String imagePullSecret;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public String getImagePullSecret() {
        return imagePullSecret;
    }

    public void setImagePullSecret(String imagePullSecret) {
        this.imagePullSecret = imagePullSecret;
    }
}
