package com.codemind.config;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.*;
/**
 * @Author qxy
 * @Date 2025/10/16 23:51
 * @Version 1.0
 */

@Data
public class CodeMindConfig {

    @Data
    public static class AIConfig {
        private String provider = "openai";
        private String apiKey;
        private String baseUrl;
        private String model = "gpt-4";
        private double temperature = 0.2;
        private int maxTokens = 4000;
        private boolean streaming = true;

        @JsonProperty("review_prompt")
        private String reviewPrompt = """
            你是一个资深的代码审查专家。请严格审查以下代码变更：
            
            ## 审查要点
            1. **代码质量**: 可读性、可维护性、代码风格
            2. **安全性**: 潜在的安全漏洞、注入风险
            3. **性能**: 性能瓶颈、资源泄漏
            4. **架构**: 设计模式、模块化程度
            5. **测试**: 测试覆盖率、边界情况
            6. **最佳实践**: 语言特性使用、错误处理
            
            ## 输出格式
            ### ✅ 优点
            - 列出代码中的优秀实践
            
            ### ⚠️ 问题与建议
            - **问题类型**: 具体问题描述
              - 建议修复方案
              - 相关代码位置
            
            ### 📊 总体评价
            - 风险等级: [低/中/高]
            - 推荐操作: [直接合并/需要修改/拒绝合并]
            """;
    }

    @Data
    public static class VCSConfig {
        private String type; // github, gitlab, gitee
        private String baseUrl;
        private String token;

        @JsonProperty("webhook_secret")
        private String webhookSecret;

        @JsonProperty("webhook_port")
        private int webhookPort = 8080;

        @JsonProperty("target_repos")
        private List<String> targetRepos = new ArrayList<>();
    }

    @Data
    public static class NotificationConfig {
        private boolean enabled = true;

        @JsonProperty("wecom")
        private WeComConfig weCom = new WeComConfig();

        @JsonProperty("slack")
        private SlackConfig slack = new SlackConfig();

        @JsonProperty("dingtalk")
        private DingTalkConfig dingTalk = new DingTalkConfig();

        @Data
        public static class WeComConfig {
            @JsonProperty("webhook_url")
            private String webhookUrl;

            @JsonProperty("mentioned_list")
            private String[] mentionedList;

            @JsonProperty("mentioned_mobile_list")
            private String[] mentionedMobileList;
        }

        @Data
        public static class SlackConfig {
            @JsonProperty("webhook_url")
            private String webhookUrl;
            private String channel;
            private String username = "CodeMind";
        }

        @Data
        public static class DingTalkConfig {
            @JsonProperty("webhook_url")
            private String webhookUrl;
            private String secret;
        }
    }

    @Data
    public static class MCPConfig {
        private boolean enabled = true;
        private List<MCPServerConfig> servers = new ArrayList<>();

        @Data
        public static class MCPServerConfig {
            private String name;
            private String command;
            private Map<String, String> env = new HashMap<>();
            private Map<String, Object> args = new HashMap<>();

            @JsonProperty("auto_start")
            private boolean autoStart = true;
        }
    }

    @Data
    public static class UIConfig {
        private String theme = "dark";
        private boolean streaming = true;

        @JsonProperty("show_progress")
        private boolean showProgress = true;

        @JsonProperty("color_scheme")
        private String colorScheme = "default";
    }

    @Data
    public static class ReviewRules {
        @JsonProperty("check_security")
        private boolean checkSecurity = true;

        @JsonProperty("check_performance")
        private boolean checkPerformance = true;

        @JsonProperty("check_code_style")
        private boolean checkCodeStyle = true;

        @JsonProperty("check_tests")
        private boolean checkTests = true;

        @JsonProperty("ignored_files")
        private List<String> ignoredFiles = Arrays.asList(
                "*.md", "*.json", "*.yaml", "*.yml",
                "*.png", "*.jpg", "*.jpeg", "*.gif"
        );

        @JsonProperty("max_review_files")
        private int maxReviewFiles = 50;

        @JsonProperty("max_file_size_kb")
        private int maxFileSizeKB = 500;
    }

    @Data
    public static class SessionConfig {
        @JsonProperty("auto_save")
        private boolean autoSave = true;

        @JsonProperty("max_sessions")
        private int maxSessions = 100;

        @JsonProperty("session_timeout_minutes")
        private int sessionTimeoutMinutes = 60;
    }

    // 主配置字段
    private AIConfig ai = new AIConfig();
    private VCSConfig vcs = new VCSConfig();
    private NotificationConfig notification = new NotificationConfig();
    private MCPConfig mcp = new MCPConfig();
    private UIConfig ui = new UIConfig();
    private ReviewRules rules = new ReviewRules();
    private SessionConfig session = new SessionConfig();
}
