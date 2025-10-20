package com.codemind.config;

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



    public void loadConfig(String configPath) {
    }
}
