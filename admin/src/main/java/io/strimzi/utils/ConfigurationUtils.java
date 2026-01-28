/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.utils;

import io.strimzi.configuration.ConfigurationConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigurationUtils {

    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String CONFIG_FOLDER_NAME = ".admin_client/";
    private static final String CONFIG_FOLDER_PATH_DEFAULT = HOME_DIR + "/" + CONFIG_FOLDER_NAME;
    private static final String CONFIG_FILE_NAME = "config.properties";

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

    /**
     * Method for creating configuration folder and config.properties file in it
     */
    public static void createConfigurationFolderAndFile() {
        try {
            Files.createDirectories(Paths.get(getConfigFolderPath()));
            Files.createFile(Paths.get(getConfigFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create folders and configuration file due to: " + e);
        }
    }

    /**
     * Check if configuration file exists
     * @return boolean determining the existence of the config.properties file
     */
    public static boolean configurationFileExists() {
        return configurationFileExists(getConfigFilePath());
    }

    public static boolean configurationFileExists(String configFilePath) {
        File configFile = new File(configFilePath);

        return configFile.exists();
    }

    /**
     * Checks whether properties for the admin-client exists.
     * If yes, this properties file is loaded and returned.
     * Otherwise, empty {@link Properties} object is returned.
     *
     * @return  {@link Properties} object with configuration of admin-client
     */
    public static Properties getAdminClientPropertiesIfExists() {
        if (Files.exists(Paths.get(getConfigFilePath()))) {
            return getPropertiesFromConfigurationFile(getConfigFilePath());
        }

        return new Properties();
    }

    /**
     * Loads Properties from the config.properties file
     * @return Properties loaded from the config.properties file
     */
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

    /**
     * Loads Properties from the configuration file specified by {@code configFilePath}
     * and builds {@link Map} based on it.
     * @param configFilePath file path to the configuration file
     * @return {@link Map} with configuration from file
     */
    public static Map<String, String> getMapOfPropertiesFromConfigurationFile(String configFilePath) {
        return getPropertiesFromConfigurationFile(configFilePath).entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            )
        );
    }

    /**
     * If the configuration file doesn't exist, creates a new one (together with needed config folder)
     * After that it loads the current Properties (may be empty if the config file didn't exist) and
     * updates it with the new properties
     * Finally it stores the Properties into the config.properties file
     * @param properties new Properties that should be added to the config.properties file
     */
    public static void writeToConfigurationFile(Properties properties) {
        if (!configurationFileExists()) {
            createConfigurationFolderAndFile();
        }
        Properties currentConfiguration = getAdminClientPropertiesIfExists();
        currentConfiguration.putAll(properties);

        try (FileOutputStream fileOutputStream = new FileOutputStream(getConfigFilePath())) {
            currentConfiguration.store(fileOutputStream, "Store properties to config file");
        } catch (IOException e) {
            throw new RuntimeException("Unable to store the properties to configuration file due to: " + e);
        }
    }

    /**
     * Copies file from source destination to target destination with desired name
     * @param filePath of source file
     * @param fileNameInConfigFolder desired name of the target file
     */
    public static void copyFileToConfigurationFolder(String filePath, String fileNameInConfigFolder) {
        Path sourceFilePath = Paths.get(filePath);
        Path targetFilePath = Paths.get(getConfigFilePath() + fileNameInConfigFolder);

        try {
            Files.copy(sourceFilePath, targetFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy file from path: " + filePath + " to the configuration folder due to: ", e.getCause());
        }
    }

    /**
     * Loads all lines from the file in the configuration folder
     * @param fileName name of the file in the configuration folder
     * @return file contents in String
     */
    public static String getContentsOfTheFileInConfigFolder(String fileName) {
        Path filePath = Paths.get(getConfigFilePath() + fileName);

        try {
            return String.join("\n", Files.readAllLines(filePath));
        } catch (IOException e) {
            throw new RuntimeException("No file with name: " + fileName + " exists in the configuration folder");
        }
    }

    private static String getConfigFolderPath() {
        String specificFolderPath = System.getenv(ConfigurationConstants.CONFIG_FOLDER_PATH_ENV);
        return specificFolderPath == null || specificFolderPath.isEmpty() ? CONFIG_FOLDER_PATH_DEFAULT : specificFolderPath + "/" + CONFIG_FOLDER_NAME;
    }

    private static String getConfigFilePath() {
        return getConfigFolderPath() + CONFIG_FILE_NAME;
    }

}
