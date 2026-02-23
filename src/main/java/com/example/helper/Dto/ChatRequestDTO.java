// ChatRequestDTO.java
package com.example.helper.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "聊天请求DTO")
public class ChatRequestDTO {

    @Schema(description = "对话消息历史", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, String>> messages;

    @Schema(description = "系统提示词")
    private String systemPrompt;
}
