package com.codemind.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author qxy
 * @Date 2025/10/17 10:23
 * @Version 1.0
 */
@Slf4j
public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.codemind";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.yaml";

    private CodeMindConfig config;
    private final ObjectMapper yamlMapper;
    private final Map<String, Object> runtimeConfig = new HashMap<>();

    public ConfigManager() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        loadConfig();
    }

    public ConfigManager(String configPath) {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        loadConfig(configPath);
    }

    private void loadConfig() {
        loadConfig(CONFIG_FILE);
    }

    private void loadConfig(String configPath) {
        try {
            Path path = Paths.get(configPath);
            if (Files.exists(path)) {
                this.config = yamlMapper.readValue(path.toFile(), CodeMindConfig.class);
                log.info("Configuration loaded from: {}", configPath);
            } else {
                createDefaultConfig();
                log.info("Default configuration created at: {}", configPath);
            }
        } catch (Exception e) {
            log.error("Failed to load configuration from: {}", configPath, e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        this.config = new CodeMindConfig();
        saveConfig();
    }

    public void saveConfig() {
        saveConfig(CONFIG_FILE);
    }

    public void saveConfig(String configPath) {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            yamlMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(configPath), config);
            log.info("Configuration saved to: {}", configPath);
        } catch (Exception e) {
            log.error("Failed to save configuration to: {}", configPath, e);
        }
    }

    public CodeMindConfig getConfig() {
        return config;
    }

    public void updateConfig(CodeMindConfig newConfig) {
        this.config = newConfig;
        saveConfig();
    }

    @SuppressWarnings("unchecked")
    public <T> T getRuntimeConfig(String key, T defaultValue) {
        return (T) runtimeConfig.getOrDefault(key, defaultValue);
    }

    public void setRuntimeConfig(String key, Object value) {
        runtimeConfig.put(key, value);
    }

    public void reload() {
        loadConfig();
    }
}
