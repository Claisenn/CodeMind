package com.codemind.config;

import com.codemind.cli.output.ConsolePrinter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @Author qxy
 * @Date 2025/10/20 23:14
 * @Version 1.0
 */
//配置文件管理
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    private ConfigManager() {
        properties = new Properties();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void loadConfig(String configPath) throws IOException {
        Path path = Paths.get(configPath);

        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
        } else {
            // 尝试从类路径加载
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                } else {
                    createDefaultConfig();
                }
            }
        }
        // 检查必要的配置
        validateRequiredConfig();
    }

    private void createDefaultConfig() {
        //设置默认配置
        properties.setProperty("openai.model", "gpt-4");
        properties.setProperty("openai.temperature", "0.2");
        properties.setProperty("openai.timeout", "60");

        ConsolePrinter.printWarning("使用默认配置，请创建配置文件设置 OpenAI API Key");
    }

    private void validateRequiredConfig() {
        if (getProperty("openai.api.key") == null) {
            ConsolePrinter.printError("未设置 OpenAI API Key");
            ConsolePrinter.printInfo("请在配置文件中设置: openai.api.key=your_api_key");
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

}
