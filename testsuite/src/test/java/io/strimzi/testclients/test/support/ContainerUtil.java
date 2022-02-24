/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

package io.strimzi.testclients.test.support;

import java.util.Locale;

public class ContainerUtil {
    static String getImageName(String envVarName, String defaultName) {
        String imageName = System.getenv(envVarName);
        if (imageName == null) {
            imageName = System.getProperty(envVarName.replace("_", ".").toLowerCase(Locale.ROOT));
        }
        return imageName != null ? imageName : defaultName;
    }
}
