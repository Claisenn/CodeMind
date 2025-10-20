package com.codemind.cli;

import com.codemind.config.ConfigManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
/**
 * @Author qxy
 * @Date 2025/10/20 22:58
 * @Version 1.0
 */
@Command(
        name = "codemind",
        description = "CodeMind CLI工具",
        mixinStandardHelpOptions = true,
        version = "CodeMind CLI 1.0",
        subcommands = {

        }
)
public class CodeMindCli {
    @Option(
            names = {"--config", "-c"},
            description = "配置文件路径",
            defaultValue = "config/application.properties"
    )
    private String configPath;

    @Option(
            names = {"--verbose", "-v"},
            description = "详细输出模式"
    )
    private boolean verbose;

    private ConfigManager configManager;

/**
 * 执行命令行调用的核心方法
 * 该方法负责初始化配置管理器并加载配置文件
 * 如果没有提供子命令，则显示帮助信息
 * @return 返回状态码，0表示成功执行
 * @throws Exception 如果配置加载过程中发生错误
 */
    public Integer call() throws Exception {
        // 初始化配置管理器
        configManager = ConfigManager.getInstance();
        // 加载指定路径的配置文件
        configManager.loadConfig(configPath);

        // 如果没有子命令，显示帮助信息
        CommandLine.usage(this, System.out);
        // 返回执行状态码，0表示成功
        return 0;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isVerbose() {
        return verbose;
    }

/**
 * 程序的主入口方法
 * @param args 命令行参数数组，用于接收用户在命令行中输入的参数
 */
    public static void main(String[] args) {
        // 创建CommandLine实例，传入CodeMindCli对象，并执行传入的命令行参数
        // execute方法会返回一个退出码，表示程序执行的状态
        int exitCode = new CommandLine(new CodeMindCli()).execute(args);
        // 根据程序执行结果退出程序
        // 0表示正常退出，非0表示异常退出或其他状态
        System.exit(exitCode);
    }
}
