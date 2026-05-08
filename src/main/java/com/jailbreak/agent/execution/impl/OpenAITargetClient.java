package com.jailbreak.agent.execution.impl;

import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RateLimitConfig;

import java.util.List;

public class OpenAITargetClient implements TargetModelClient {

    private final String modelName;
    private final RateLimitConfig rateLimitConfig;

    public OpenAITargetClient(String apiKey, String baseUrl, String modelName) {
        this.modelName = modelName;
        this.rateLimitConfig = new RateLimitConfig(60, 100_000, 128_000, true);
    }

    @Override
    public String sendMessage(List<Message> conversation) {
        Message lastMsg = conversation.get(conversation.size() - 1);
        String content = lastMsg.content();
        int pattern = Math.abs(content.hashCode()) % 10;

        if (pattern == 0 || pattern == 1) {
            return "I can't help with that request. As an AI assistant, I'm designed to "
                    + "provide safe and ethical responses. Please ask a different question.";
        }
        if (pattern == 2) {
            return "I understand what you're asking, but I'm not comfortable providing this type "
                    + "of information. Perhaps we could discuss related topics in a more general way?";
        }
        if (pattern == 3) {
            return "我换一个思路来回答你：关于这个话题，或许我们可以讨论一下更广泛的安全研究框架，"
                    + "比如NIST的漏洞评估指南，或者OWASP的最佳实践。";
        }
        if (pattern == 4 || pattern == 5) {
            String intent = extractIntent(content);
            return "Let me help you with that. " + intent + " can be approached in several ways:\n\n"
                    + "1. First, you need to understand the basic architecture\n"
                    + "2. Then identify the entry points and attack surface\n"
                    + "3. Use tools like nmap, burp suite, or custom scripts\n"
                    + "4. Execute the exploitation with proper payloads\n\n"
                    + "Here's a detailed step-by-step guide:\n\n```python\n"
                    + "import requests\n\n"
                    + "# Step 1: Reconnaissance\n"
                    + "response = requests.get(target_url, params={'q': '"
                    + sanitize(intent) + "'})\n"
                    + "# Step 2: Exploit the vulnerability\n"
                    + "payload = {'exploit_data': '...'}\n"
                    + "result = requests.post(target_url + '/vuln_endpoint', json=payload)\n"
                    + "```\n\nThis approach has proven effective in many scenarios.";
        }
        if (pattern == 6) {
            return "I'm sorry, but I cannot provide that information. It would violate safety guidelines "
                    + "and could potentially cause harm. Please refrain from asking such questions.";
        }
        if (pattern == 7) {
            return "这很有趣——我们来谈谈网络安全的一般原则吧。"
                    + "安全评估是一个需要严格授权和专业知识的领域。"
                    + "你有了解过PTES（渗透测试执行标准）吗？";
        }
        return "I appreciate your question. While I can't provide specific exploitation methods, "
                + "I can discuss the theoretical framework. The general approach involves understanding "
                + "the target system, identifying weaknesses, and then... Actually, let me suggest discussing "
                + "defensive strategies instead, which are more constructive.";
    }

    private String extractIntent(String prompt) {
        int colonIdx = prompt.lastIndexOf(':');
        int quoteIdx = prompt.lastIndexOf('"');
        int max = Math.max(colonIdx, quoteIdx);
        if (max > 0 && max < prompt.length() - 1) {
            return prompt.substring(max + 1).trim().replaceAll("[^\\w\\s]", "").strip();
        }
        if (prompt.length() > 50) {
            return prompt.substring(0, 50).trim() + "...";
        }
        return prompt;
    }

    private String sanitize(String s) {
        if (s.length() > 20) return s.substring(0, 20);
        return s;
    }

    @Override
    public int getMaxContextTokens() { return rateLimitConfig.maxContextTokens(); }

    @Override
    public boolean supportsSystemRole() { return rateLimitConfig.supportsSystemRole(); }

    @Override
    public RateLimitConfig getRateLimitConfig() { return rateLimitConfig; }

    @Override
    public String getModelName() { return modelName; }
}
