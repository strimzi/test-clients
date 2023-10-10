/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

public class ConfigurationUtils {

    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String CONFIG_FOLDER_PATH = HOME_DIR + ".admin_client/";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + CONFIG_FILE_NAME;

    /**
     * Method that takes {@param property} and transforms it to "properties file format"
     * In case env variable is passed to the method, it transforms it to lower case and replaces the "_" to "."
     * - f.e. "MY_ENV" will be transformed to "my.env"
     * In case when the {@param property} is already "transformed" to the format, it does nothing
     * @param property that we want to transform
     * @return transformed property in lower case and with "." instead of "_"
     */
    public static String transformPropertyToPropertiesFormat(String property) {
        return property.toLowerCase(Locale.ROOT).replaceAll("_", ".");
    }

    /**
     * Method that takes {@param property} and transforms it to "environment variable format"
     * In case property from properties file is passed to the method, it transforms it to upper case and replaces the "." to "_"
     * - f.e. "my.env" will be transformed to "MY_ENV"
     * In case when the {@param property} is already "transformed" to the format, it does nothing
     * @param property that we want to transform
     * @return transformed property in upper case and with "_" instead of "."
     */
    public static String transformPropertyToEnvFormat(String property) {
        return property.toUpperCase(Locale.ROOT).replaceAll("\\.", "_");
    }

    public static void createConfigurationFolderAndFile() {
        Path configurationFilePath = Paths.get(CONFIG_FILE_PATH);

        try {
            Files.createDirectories(configurationFilePath);
            Files.createFile(configurationFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create folders and configuration file due to: ", e.getCause());
        }
    }

    public static boolean configurationFileExists() {
        return configurationFileExists(CONFIG_FILE_PATH);
    }

    public static boolean configurationFileExists(String configFilePath) {
        File configFile = new File(configFilePath);

        return configFile.exists();
    }

    public static Properties getPropertiesFromConfigurationFile() {
        return getPropertiesFromConfigurationFile(CONFIG_FILE_PATH);
    }

    public static Properties getPropertiesFromConfigurationFile(String configFilePath) {
        File configFile = new File(configFilePath);
        Properties configuration = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            configuration.load(fileInputStream);
        } catch (IOException e) {
            System.out.println("Failed to load configuration file - no such file exist");
        }
        return configuration;
    }

    public static void writeToConfigurationFile(Properties properties) {
        if (!configurationFileExists()) {
            createConfigurationFolderAndFile();
        }
        Properties currentConfiguration = getPropertiesFromConfigurationFile();
        currentConfiguration.putAll(properties);

        try (FileOutputStream fileOutputStream = new FileOutputStream(CONFIG_FILE_PATH)) {
            currentConfiguration.store(fileOutputStream, "Store properties to config file");
        } catch (IOException e) {
            throw new RuntimeException("Unable to store the properties to configuration file due to: ", e.getCause());
        }
    }

    public static void copyFileToConfigurationFolder(String filePath, String fileNameInConfigFolder) {
        Path sourceFilePath = Paths.get(filePath);
        Path targetFilePath = Paths.get(CONFIG_FOLDER_PATH + fileNameInConfigFolder);

        try {
            Files.copy(sourceFilePath, targetFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy file from path: " + filePath + " to the configuration folder due to: ", e.getCause());
        }
    }

    public static String getContentsOfTheFileInConfigFolder(String fileName) {
        Path filePath = Paths.get(CONFIG_FOLDER_PATH + fileName);

        try {
            return String.join("\n", Files.readAllLines(filePath));
        } catch (IOException e) {
            throw new RuntimeException("No file with name: " + fileName + " exists in the configuration folder");
        }
    }
}
