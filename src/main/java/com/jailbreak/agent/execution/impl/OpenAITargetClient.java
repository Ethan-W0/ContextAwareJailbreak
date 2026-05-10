package com.jailbreak.agent.execution.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.model.LLMConfig;
import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RateLimitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OpenAITargetClient implements TargetModelClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAITargetClient.class);

    private final String apiKey;
    private final String baseUrl;
    private final String modelName;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final int maxRetries;
    private final Duration timeout;

    public OpenAITargetClient(String apiKey, String baseUrl, String modelName,
                               int maxRetries, long timeoutSeconds) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.modelName = modelName;
        this.maxRetries = maxRetries;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.rateLimitConfig = new RateLimitConfig(60, 100_000, 128_000, true);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String sendMessage(List<Message> conversation) {
        return sendWithRetry(conversation, 1, null);
    }

    @Override
    public String sendMessage(List<Message> conversation, LLMConfig config) {
        if (config == null || (config.apiKey() == null && config.baseUrl() == null)) {
            return sendMessage(conversation);
        }
        return sendWithRetry(conversation, 1, config);
    }

    private String sendWithRetry(List<Message> conversation, int retryCount, LLMConfig config) {
        try {
            String effectiveApiKey = config != null && config.apiKey() != null ? config.apiKey() : apiKey;
            String effectiveBaseUrl = config != null && config.baseUrl() != null ? config.baseUrl() : baseUrl;
            String effectiveModel = config != null && config.modelName() != null ? config.modelName() : modelName;
            String effectiveBase = effectiveBaseUrl.endsWith("/")
                    ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1) : effectiveBaseUrl;

            String requestBody = buildRequestBody(conversation, effectiveModel);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(effectiveBase + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + effectiveApiKey)
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                return node.path("choices").get(0)
                        .path("message").path("content").asText();
            }

            if (statusCode == 429) {
                return handleRateLimit(response, conversation, retryCount, config);
            }

            if (statusCode >= 500 && retryCount <= maxRetries) {
                log.warn("Target API server error ({}), retrying {}/{}",
                        statusCode, retryCount, maxRetries);
                Thread.sleep(2000L * retryCount);
                return sendWithRetry(conversation, retryCount + 1, config);
            }

            throw new RuntimeException("Target API returned status " + statusCode
                    + ": " + response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Target API call interrupted", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (retryCount <= maxRetries) {
                log.warn("Target API call failed ({}), retrying {}/{}",
                        e.getMessage(), retryCount, maxRetries);
                try { Thread.sleep(1000L * retryCount); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return sendWithRetry(conversation, retryCount + 1, config);
            }
            throw new RuntimeException("Target API call failed after " + maxRetries + " retries", e);
        }
    }

    private String handleRateLimit(HttpResponse<String> response,
                                    List<Message> conversation, int retryCount, LLMConfig config) {
        if (retryCount > maxRetries) {
            throw new RuntimeException("Rate limited after " + maxRetries + " retries");
        }

        long waitSeconds = 5;
        String retryAfter = response.headers().firstValue("Retry-After").orElse(null);
        if (retryAfter != null) {
            try {
                waitSeconds = Long.parseLong(retryAfter);
            } catch (NumberFormatException ignored) {}
        } else {
            waitSeconds = (long) Math.pow(2, retryCount);
        }

        log.warn("Rate limited (429), waiting {}s before retry {}/{}",
                waitSeconds, retryCount, maxRetries);
        try {
            Thread.sleep(waitSeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return sendWithRetry(conversation, retryCount + 1, config);
    }

    private String buildRequestBody(List<Message> conversation, String effectiveModel) throws Exception {
        var messages = conversation.stream()
                .map(m -> Map.of("role", mapRole(m.role()), "content", m.content()))
                .toList();

        var body = Map.of(
                "model", effectiveModel,
                "messages", messages,
                "max_tokens", 2048,
                "temperature", 0.7
        );
        return objectMapper.writeValueAsString(body);
    }

    private String mapRole(String role) {
        return switch (role.toLowerCase()) {
            case "user" -> "user";
            case "assistant" -> "assistant";
            case "system" -> "system";
            default -> "user";
        };
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
