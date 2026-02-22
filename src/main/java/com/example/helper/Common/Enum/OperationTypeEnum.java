// OperationTypeEnum.java
package com.example.helper.Common.Enum;

import lombok.Getter;

/**
 * 操作类型枚举
 */
@Getter
public enum OperationTypeEnum {
    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    QUERY("QUERY", "查询"),
    ANALYZE("ANALYZE", "分析"),
    LOGIN("LOGIN", "登录"),
    LOGOUT("LOGOUT", "登出");

    private final String code;
    private final String description;

    OperationTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
