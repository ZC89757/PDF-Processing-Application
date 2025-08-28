package com.app.pdf.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiClient {
    
    @Value("${ai.model.name:default-model}")
    private String modelName;
    
    @Value("${ai.api.key:default-key}")
    private String apiKey;
    
    public String generateSummary(String prompt) {
        // 这里应该是实际调用AI服务的代码
        // 为简化实现，返回模拟摘要
        return "这是AI生成的文档摘要。实际实现应该调用大模型API来生成真实的摘要内容。";
    }
}