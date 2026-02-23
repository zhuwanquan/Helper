// AIController.java
package com.example.helper.Controller;

import com.example.helper.Service.AIService;
import com.example.helper.Dto.ChatRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI服务", description = "DeepSeek AI相关接口")
public class AIController {

    private final AIService aiService;
    private final Executor ioTaskExecutor;

    /**
     * 流式聊天接口
     * @param message 用户消息
     * @param systemPrompt 系统提示词（可选）
     * @return SSE流式响应
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天", description = "与DeepSeek AI进行流式对话")
    public SseEmitter chatStream(
            @Parameter(description = "用户消息") @RequestParam String message,
            @Parameter(description = "系统提示词") @RequestParam(required = false) String systemPrompt) {

        log.info("收到流式聊天请求: message={}, systemPrompt={}", message, systemPrompt);

        // 创建SSE Emitter，设置超时时间为60秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 构建用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);

        List<Map<String, String>> messages = List.of(userMessage);

        // 异步处理流式响应
        CompletableFuture.runAsync(() -> {
            try {
                aiService.chatStream(messages, systemPrompt)
                        .subscribe(
                                // onNext: 发送数据块
                                content -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(content));
                                    } catch (IOException e) {
                                        log.error("发送SSE事件失败: {}", e.getMessage(), e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                // onError: 处理错误
                                error -> {
                                    log.error("AI服务出错: {}", error.getMessage(), error);
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("error")
                                                .data("处理请求时发生错误: " + error.getMessage()));
                                    } catch (IOException e) {
                                        log.error("发送错误事件失败: {}", e.getMessage(), e);
                                    }
                                    emitter.complete();
                                },
                                // onComplete: 完成处理
                                () -> {
                                    log.info("流式响应完成");
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("complete")
                                                .data("[DONE]"));
                                    } catch (IOException e) {
                                        log.error("发送完成事件失败: {}", e.getMessage(), e);
                                    }
                                    emitter.complete();
                                }
                        );
            } catch (Exception e) {
                log.error("启动流式处理失败: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        }, ioTaskExecutor);

        // 设置完成回调
        emitter.onCompletion(() -> log.info("SSE连接完成"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError(error -> log.error("SSE连接出错: {}", error.getMessage(), error));

        return emitter;
    }

    /**
     * 多轮对话流式接口
     * @param messages 对话历史消息
     * @param systemPrompt 系统提示词（可选）
     * @return SSE流式响应
     */
    @PostMapping(value = "/chat/conversation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "多轮对话流式", description = "支持多轮对话的流式AI接口")
    public SseEmitter conversationStream(
            @Parameter(description = "对话消息历史") @RequestBody List<Map<String, String>> messages,
            @Parameter(description = "系统提示词") @RequestParam(required = false) String systemPrompt) {

        log.info("收到多轮对话流式请求，消息数量: {}", messages.size());

        // 创建SSE Emitter
        SseEmitter emitter = new SseEmitter(60000L);

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                aiService.chatStream(messages, systemPrompt)
                        .subscribe(
                                content -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(content));
                                    } catch (IOException e) {
                                        log.error("发送SSE事件失败: {}", e.getMessage(), e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> handleError(emitter, error, "多轮对话"),
                                () -> handleComplete(emitter, "多轮对话")
                        );
            } catch (Exception e) {
                log.error("启动多轮对话流式处理失败: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        }, ioTaskExecutor);

        // 设置回调
        setupEmitterCallbacks(emitter, "多轮对话");

        return emitter;
    }

    /**
     * 多轮对话流式接口 - 使用 DTO 版本
     * @param chatRequest 聊天请求DTO
     * @return SSE流式响应
     */
    @PostMapping(value = "/chat/conversation-dto", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "多轮对话流式(DTO)", description = "使用DTO的多轮对话流式AI接口")
    public SseEmitter conversationStreamWithDTO(
            @Parameter(description = "聊天请求") @RequestBody ChatRequestDTO chatRequest) {
        
        log.info("收到DTO多轮对话流式请求，消息数量: {}", 
                 chatRequest.getMessages() != null ? chatRequest.getMessages().size() : 0);
        
        // 创建SSE Emitter
        SseEmitter emitter = new SseEmitter(60000L);
        
        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                aiService.chatStream(chatRequest.getMessages(), chatRequest.getSystemPrompt())
                        .subscribe(
                                content -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(content));
                                    } catch (IOException e) {
                                        log.error("发送SSE事件失败: {}", e.getMessage(), e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> handleError(emitter, error, "DTO多轮对话"),
                                () -> handleComplete(emitter, "DTO多轮对话")
                        );
            } catch (Exception e) {
                log.error("启动DTO多轮对话流式处理失败: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        }, ioTaskExecutor);
        
        // 设置回调
        setupEmitterCallbacks(emitter, "DTO多轮对话");
        
        return emitter;
    }

    /**
     * 处理错误情况
     */
    private void handleError(SseEmitter emitter, Throwable error, String context) {
        log.error("{} AI服务出错: {}", context, error.getMessage(), error);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("处理请求时发生错误: " + error.getMessage()));
        } catch (IOException e) {
            log.error("发送错误事件失败: {}", e.getMessage(), e);
        }
        emitter.complete();
    }

    /**
     * 处理完成情况
     */
    private void handleComplete(SseEmitter emitter, String context) {
        log.info("{}流式响应完成", context);
        try {
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("[DONE]"));
        } catch (IOException e) {
            log.error("发送完成事件失败: {}", e.getMessage(), e);
        }
        emitter.complete();
    }

    /**
     * 设置Emitter回调
     */
    private void setupEmitterCallbacks(SseEmitter emitter, String context) {
        emitter.onCompletion(() -> log.info("{}SSE连接完成", context));
        emitter.onTimeout(() -> {
            log.warn("{}SSE连接超时", context);
            emitter.complete();
        });
        emitter.onError(error -> log.error("{}SSE连接出错: {}", context, error.getMessage(), error));
    }
}
