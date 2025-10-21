package com.codemind.cli.output;

/**
 * @Author qxy
 * @Date 2025/10/20 23:03
 * @Version 1.0
 */
//控制台输出
public class ConsolePrinter {
    // ANSI 颜色代码
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
/**
 * 打印警告信息的静态方法
 * @param message 要打印的警告信息字符串
 */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

/**
 * 打印错误信息的方法
 * @param message 要打印的错误信息字符串
 */
    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }

/**
 * 打印带有蓝色信息图标的信息
 * @param message 需要打印的信息内容
 */
    public static void printInfo(String message) {
        // 使用System.out.println打印信息
        // BLUE是蓝色控制码，ℹ是信息图标，RESET是重置控制码
        System.out.println(BLUE + "ℹ " + message + RESET);
    }

/**
 * 打印成功消息的方法
 * @param message 要打印的成功消息内容
 */
    public static void printSuccess(String message) {
        // 使用绿色输出成功标记和消息
        System.out.println(GREEN + "✓ " + message + RESET);
    }
/**
 * 该方法会打印一个格式化的标题，包含上下两行分隔线和居中的标题文本
 *
 * @param title 要显示在分割部分中的标题文本
 */
    public static void printSection(String title) {
        System.out.println();
        // 打印60个等号作为上边框，使用CYAN颜色
        System.out.println(CYAN + "═".repeat(60) + RESET);
        // 打印标题文本，前后各加一个空格使其居中，使用PURPLE颜色
        System.out.println(PURPLE + " " + title + RESET);
        // 打印60个等号作为下边框，使用CYAN颜色
        System.out.println(CYAN + "═".repeat(60) + RESET);
    }

/**
 * 打印文件审查结果的方法
 * @param fileName 要审查的文件名
 * @param result 审查结果字符串
 */
    public static void printReviewResult(String fileName, String result) {
        printSection("审查结果: " + fileName);
        System.out.println(result);
    }

/**
 * 打印代码块的格式化输出方法
 * 将输入的代码字符串用特定的格式包裹后输出
 * @param code 要打印的代码字符串
 */
    public static void printCodeBlock(String code) {
        System.out.println(YELLOW + "```java");
        System.out.println(code);
        System.out.println("```" + RESET);
    }
}
