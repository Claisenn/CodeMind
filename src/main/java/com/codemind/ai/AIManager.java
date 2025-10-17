package com.codemind.ai;

import com.codemind.config.CodeMindConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @Author qxy
 * @Date 2025/10/17 11:17
 * @Version 1.0
 */


@Slf4j
public class AIManager {
    private final CodeMindConfig.AIConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;
    private final Map<String, AIClient> clients;

    public AIManager(CodeMindConfig.AIConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient();
        this.mapper = new ObjectMapper();
        this.clients = new ConcurrentHashMap<>();
        initializeClients();
    }

    public interface StreamingCallback {
        void onChunk(String content);
        void onComplete();
        void onError(Throwable error);
    }

    private void initializeClients() {
        // 初始化支持的AI客户端
        registerClient("openai", new OpenAIClient(config, httpClient, mapper));
        registerClient("anthropic", new AnthropicClient(config, httpClient, mapper));
        // 可以扩展更多提供商
    }

    private void registerClient(String provider, AIClient client) {
        clients.put(provider.toLowerCase(), client);
    }

    public CompletableFuture<String> chat(String provider, List<Message> messages) {
        AIClient client = clients.get(provider.toLowerCase());
        if (client == null) {
            throw new IllegalArgumentException("不支持的AI提供商: " + provider);
        }
        return client.chat(messages);
    }

    public void streamChat(String provider, List<Message> messages, StreamingCallback callback) {
        AIClient client = clients.get(provider.toLowerCase());
        if (client == null) {
            throw new IllegalArgumentException("不支持的AI提供商: " + provider);
        }

        if (!config.isStreaming()) {
            // 如果不支持流式，回退到普通聊天
            chat(provider, messages).thenAccept(result -> {
                callback.onChunk(result);
                callback.onComplete();
            }).exceptionally(error -> {
                callback.onError(error);
                return null;
            });
            return;
        }

        client.streamChat(messages, callback);
    }

    public List<String> getAvailableProviders() {
        return new ArrayList<>(clients.keySet());
    }

    // 消息类
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;

        public static Message user(String content) {
            return new Message("user", content);
        }

        public static Message assistant(String content) {
            return new Message("assistant", content);
        }

        public static Message system(String content) {
            return new Message("system", content);
        }
    }
}

// AI客户端接口
interface AIClient {
    CompletableFuture<String> chat(List<AIManager.Message> messages);
    void streamChat(List<AIManager.Message> messages, AIManager.StreamingCallback callback);
}

// OpenAI客户端实现
@Slf4j
class OpenAIClient implements AIClient {
    private final CodeMindConfig.AIConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public OpenAIClient(CodeMindConfig.AIConfig config, OkHttpClient httpClient, ObjectMapper mapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.openai.com/v1";
    }

    @Override
    public CompletableFuture<String> chat(List<AIManager.Message> messages) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> request = createRequest(messages, false);
                Request httpRequest = buildRequest(request);

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP " + response.code() + ": " + response.body().string());
                    }

                    String responseBody = response.body().string();
                    return extractContent(responseBody);
                }
            } catch (Exception e) {
                log.error("OpenAI API调用失败", e);
                throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void streamChat(List<AIManager.Message> messages, AIManager.StreamingCallback callback) {
        try {
            Map<String, Object> request = createRequest(messages, true);
            Request httpRequest = buildRequest(request);

            httpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("HTTP " + response.code()));
                        return;
                    }

                    try (ResponseBody body = response.body()) {
                        processStreamResponse(body, callback);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private Map<String, Object> createRequest(List<AIManager.Message> messages, boolean stream) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", config.getModel());
        request.put("messages", convertMessages(messages));
        request.put("temperature", config.getTemperature());
        request.put("max_tokens", config.getMaxTokens());
        request.put("stream", stream);
        return request;
    }

    private List<Map<String, String>> convertMessages(List<AIManager.Message> messages) {
        return messages.stream()
                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                .toList();
    }

    private Request buildRequest(Map<String, Object> request) throws Exception {
        return new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        mapper.writeValueAsString(request),
                        MediaType.get("application/json")
                ))
                .build();
    }

    private String extractContent(String responseBody) throws Exception {
        Map<String, Object> response = mapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        }
        throw new IOException("无效的API响应格式");
    }

    private void processStreamResponse(ResponseBody body, AIManager.StreamingCallback callback) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(body.byteStream()));

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                    String json = line.substring(6);
                    processStreamChunk(json, callback);
                }
            }
            callback.onComplete();
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private void processStreamChunk(String json, AIManager.StreamingCallback callback) {
        try {
            Map<String, Object> chunk = mapper.readValue(json, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                if (delta != null && delta.containsKey("content")) {
                    String content = (String) delta.get("content");
                    callback.onChunk(content);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误，继续处理后续数据
        }
    }
}

// Anthropic客户端实现
@Slf4j
class AnthropicClient implements AIClient {
    private final CodeMindConfig.AIConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public AnthropicClient(CodeMindConfig.AIConfig config, OkHttpClient httpClient, ObjectMapper mapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public CompletableFuture<String> chat(List<AIManager.Message> messages) {
        // 实现Anthropic API调用
        return CompletableFuture.completedFuture("Anthropic API待实现");
    }

    @Override
    public void streamChat(List<AIManager.Message> messages, AIManager.StreamingCallback callback) {
        // 实现Anthropic流式API调用
        callback.onError(new UnsupportedOperationException("Anthropic流式API待实现"));
    }
}
