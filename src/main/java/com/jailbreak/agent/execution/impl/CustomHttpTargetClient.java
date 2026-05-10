package com.jailbreak.agent.execution.impl;

import com.jailbreak.agent.execution.TargetModelClient;
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

public class CustomHttpTargetClient implements TargetModelClient {

    private static final Logger log = LoggerFactory.getLogger(CustomHttpTargetClient.class);

    private final String endpointUrl;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final String requestTemplate;
    private final String responseJsonPath;
    private final RateLimitConfig rateLimitConfig;
    private final HttpClient httpClient;
    private final int maxRetries;
    private final String modelName;

    public CustomHttpTargetClient(String endpointUrl,
                                   Map<String, String> headers,
                                   Map<String, String> cookies,
                                   String requestTemplate,
                                   String responseJsonPath,
                                   int maxRetries,
                                   long timeoutSeconds) {
        this.endpointUrl = endpointUrl;
        this.headers = headers != null ? Map.copyOf(headers) : Map.of();
        this.cookies = cookies != null ? Map.copyOf(cookies) : Map.of();
        this.requestTemplate = requestTemplate != null ? requestTemplate : "{\"prompt\": \"{{PROMPT}}\"}";
        this.responseJsonPath = responseJsonPath != null ? responseJsonPath : "$.response";
        this.maxRetries = maxRetries;
        this.modelName = "custom-http";
        this.rateLimitConfig = new RateLimitConfig(30, 50_000, 64_000, false);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String sendMessage(List<Message> conversation) {
        return sendWithRetry(conversation, 1);
    }

    private String sendWithRetry(List<Message> conversation, int retryCount) {
        try {
            Message lastMsg = conversation.get(conversation.size() - 1);
            String requestBody = buildRequestBody(lastMsg.content());

            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(30));

            requestBuilder.header("Content-Type", "application/json");
            headers.forEach(requestBuilder::header);

            if (!cookies.isEmpty()) {
                String cookieStr = cookies.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("");
                requestBuilder.header("Cookie", cookieStr);
            }

            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return extractResponse(response.body());
            }
            if (response.statusCode() == 429) {
                if (retryCount > maxRetries) {
                    throw new RuntimeException("Rate limited after " + maxRetries + " retries");
                }
                long waitSeconds = (long) Math.pow(2, retryCount);
                Thread.sleep(waitSeconds * 1000);
                return sendWithRetry(conversation, retryCount + 1);
            }
            throw new RuntimeException("Custom endpoint returned status "
                    + response.statusCode() + ": " + response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Custom HTTP call interrupted", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (retryCount <= maxRetries) {
                log.warn("Custom HTTP call failed, retrying {}/{}: {}",
                        retryCount, maxRetries, e.getMessage());
                try { Thread.sleep(1000L * retryCount); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return sendWithRetry(conversation, retryCount + 1);
            }
            throw new RuntimeException("Custom HTTP call failed after " + maxRetries + " retries", e);
        }
    }

    private String buildRequestBody(String prompt) {
        return requestTemplate.replace("{{PROMPT}}", escapeJson(prompt));
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractResponse(String responseBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(responseBody);

            String path = responseJsonPath;
            if (path.startsWith("$.")) path = path.substring(2);
            for (String segment : path.split("\\.")) {
                if (node == null) break;
                if (segment.matches("\\[\\d+\\]")) {
                    int idx = Integer.parseInt(segment.substring(1, segment.length() - 1));
                    node = node.get(idx);
                } else {
                    node = node.get(segment);
                }
            }
            return node != null ? node.asText() : responseBody;
        } catch (Exception e) {
            log.warn("Failed to extract response using path '{}', returning raw body", responseJsonPath);
            return responseBody;
        }
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
