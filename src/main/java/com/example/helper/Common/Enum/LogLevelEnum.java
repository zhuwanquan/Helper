// LogLevelEnum.java
package com.example.helper.Common.Enum;

import lombok.Getter;

/**
 * 日志级别枚举
 */

@Getter
public enum LogLevelEnum {
    TRACE("TRACE", "跟踪级别"),
    DEBUG("DEBUG", "调试级别"),
    INFO("INFO", "信息级别"),
    WARN("WARN", "警告级别"),
    ERROR("ERROR", "错误级别");

    private final String code;
    private final String description;

    LogLevelEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
