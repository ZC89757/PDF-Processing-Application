package com.app.pdf.service;

import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiClient {
    
    @Value("${ai.model.name:qwen-turbo}")
    private String modelName;
    
    @Value("${ai.api-key:}")
    private String apiKey;
    
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public String generateSummary(String prompt) {
        try {
            // 构造请求体
            Map<String, Object> input = new HashMap<>();
            Map<String, Object> messages = new HashMap<>();
            
            // 系统消息
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的文档摘要生成助手，请根据用户提供的文档内容生成简洁明了的摘要。");
            
            // 用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            
            messages.put("messages", List.of(systemMsg, userMsg));
            input.put("input", messages);
            
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("model", modelName);
            paramMap.put("input", messages);
            paramMap.put("parameters", Map.of("result_format", "message"));
            
            String requestBody = JsonUtils.toJson(paramMap);
            
            // 创建请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 解析响应
            if (response.statusCode() == 200) {
                Map responseMap = JsonUtils.fromJson(response.body(), Map.class);
                Map<String, Object> output = (Map<String, Object>) responseMap.get("output");
                List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            } else {
                return "摘要生成失败，HTTP状态码: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "摘要生成失败: " + e.getMessage();
        }
    }
}