// AIService.java
package com.example.helper.Service;

import com.example.helper.Config.DeepSeekConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper;

    /**
     * 流式聊天
     * @param messages 消息列表
     * @param systemPrompt 系统提示词
     * @return 流式响应
     */
    public Flux<String> chatStream(List<Map<String, String>> messages, String systemPrompt) {
        WebClient webClient = WebClient.builder()
                .baseUrl(deepSeekConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekConfig.getApiKey())
                .build();

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekConfig.getDefaultModel());

        // 构建消息数组
        List<Map<String, String>> fullMessages = buildMessages(messages, systemPrompt);
        requestBody.put("messages", fullMessages);
        requestBody.put("stream", true);

        log.info("发送流式请求到DeepSeek API: {}", deepSeekConfig.getBaseUrl());

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(deepSeekConfig.getReadTimeout()))
                .doOnNext(chunk -> log.debug("收到响应块: {}", chunk))
                .flatMap(chunk -> {
                    try {
                        // 解析SSE数据
                        String content = parseStreamChunk(chunk);
                        if (content != null && !content.isEmpty()) {
                            return Flux.just(content);
                        }
                        return Flux.empty();
                    } catch (Exception e) {
                        log.error("解析流式响应出错: {}", e.getMessage(), e);
                        return Flux.error(e);
                    }
                })
                .doOnComplete(() -> log.info("流式响应完成"))
                .doOnError(error -> log.error("流式响应出错: {}", error.getMessage(), error));
    }

    /**
     * 构建完整的消息列表
     */
    private List<Map<String, String>> buildMessages(List<Map<String, String>> userMessages, String systemPrompt) {
        // 添加系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt != null ? systemPrompt : "You are a helpful assistant.");

        // 创建新的消息列表
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(userMessages);

        return messages;
    }

    /**
     * 解析流式响应块
     */
    private String parseStreamChunk(String chunk) {
        try {
            log.debug("开始解析响应块: {}", chunk);

            // 处理SSE格式的数据 (原有的处理逻辑)
            if (chunk != null && chunk.startsWith("data: ")) {
                String jsonData = chunk.substring(6).trim();
                log.debug("提取的JSON数据: {}", jsonData);

                // 处理结束标记
                if ("[DONE]".equals(jsonData)) {
                    log.debug("收到结束标记 [DONE]");
                    return null;
                }

                // 解析JSON
                JsonNode jsonNode = objectMapper.readTree(jsonData);
                log.debug("解析的JSON节点: {}", jsonNode);

                // 提取内容 - 尝试多种可能的结构
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode choice = choices.get(0);
                    log.debug("选择节点: {}", choice);

                    // 尝试获取delta内容
                    JsonNode delta = choice.get("delta");
                    if (delta != null) {
                        JsonNode content = delta.get("content");
                        if (content != null) {
                            String result = content.asText();
                            log.debug("从delta.content提取内容: {}", result);
                            return result;
                        }
                    }

                    // 如果没有delta，尝试直接从choice获取内容
                    JsonNode message = choice.get("message");
                    if (message != null) {
                        JsonNode content = message.get("content");
                        if (content != null) {
                            String result = content.asText();
                            log.debug("从message.content提取内容: {}", result);
                            return result;
                        }
                    }

                    // 直接从choice获取content
                    JsonNode content = choice.get("content");
                    if (content != null) {
                        String result = content.asText();
                        log.debug("从choice.content提取内容: {}", result);
                        return result;
                    }
                }

                // 如果上面都没找到，尝试其他可能的结构
                JsonNode content = jsonNode.get("content");
                if (content != null) {
                    String result = content.asText();
                    log.debug("从根content提取内容: {}", result);
                    return result;
                }
            }
            // 处理直接JSON格式（DeepSeek的实际格式）
            else if (chunk != null && chunk.startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(chunk);
                log.debug("解析直接JSON节点: {}", jsonNode);

                // 检查是否是结束标记
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode choice = choices.get(0);
                    log.debug("直接JSON选择节点: {}", choice);

                    // 获取delta内容
                    JsonNode delta = choice.get("delta");
                    if (delta != null) {
                        JsonNode content = delta.get("content");
                        if (content != null) {
                            String result = content.asText();
                            // 注意：空字符串也要返回，因为可能是有效的响应
                            log.debug("从直接JSON delta.content提取内容: '{}'", result);
                            return result;
                        }
                    }
                }

                // 检查usage信息（响应结束时）
                JsonNode usage = jsonNode.get("usage");
                if (usage != null) {
                    log.debug("收到usage信息，响应即将结束: {}", usage);
                }
            }
            else if ("[DONE]".equals(chunk)) {
                log.debug("收到直接的结束标记 [DONE]");
                return null;
            }
            else {
                log.debug("响应块不符合已知格式或为空");
            }

            return null;
        } catch (Exception e) {
            log.warn("解析响应块失败: {}", chunk, e);
            return null;
        }
    }


}
