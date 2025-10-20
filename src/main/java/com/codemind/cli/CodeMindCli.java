package com.codemind.cli;

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
}
