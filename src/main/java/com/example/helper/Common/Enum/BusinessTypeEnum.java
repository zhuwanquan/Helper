// BusinessTypeEnum.java
package com.example.helper.Common.Enum;

import lombok.Getter;

/**
 * 业务类型枚举
 */
@Getter
public enum BusinessTypeEnum {
    MEAL_MANAGEMENT("MEAL", "菜品管理"),
    NUTRITION_ANALYSIS("NUTRITION", "营养分析"),
    RAG_QUERY("RAG", "RAG问答"),
    USER_AUTH("AUTH", "用户认证"),
    SYSTEM_CONFIG("CONFIG", "系统配置"),
    DATA_SYNC("SYNC", "数据同步");

    private final String code;
    private final String description;

    BusinessTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
