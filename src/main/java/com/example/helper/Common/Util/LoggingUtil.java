// LoggingUtil.java
package com.example.helper.Common.Util;

import com.example.helper.Common.Enum.BusinessTypeEnum;
import com.example.helper.Common.Enum.OperationTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Component
public class LoggingUtil {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String USER_ID_KEY = "userId";

    /**
     * 初始化MDC上下文
     */
    public static void initTraceContext(String userId) {
        MDC.put(TRACE_ID_KEY, UUID.randomUUID().toString().replace("-", ""));
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        }
    }

    /**
     * 清理MDC上下文
     */
    public static void clearTraceContext() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(USER_ID_KEY);
    }

    /**
     * 获取当前TraceId
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 记录业务日志
     */
    public static void logBusiness(String userId, BusinessTypeEnum businessType,
                                 OperationTypeEnum operationType, String message) {
        initTraceContext(userId);
        log.info("业务日志 - 类型: {}, 操作: {}, 消息: {}",
                businessType.getDescription(), operationType.getDescription(), message);
        clearTraceContext();
    }

    /**
     * 记录性能日志
     */
    public static void logPerformance(String methodName, long executeTime) {
        if (executeTime > 1000) { // 超过1秒记录警告
            log.warn("性能警告 - 方法: {}, 执行时间: {}ms", methodName, executeTime);
        } else {
            log.debug("性能日志 - 方法: {}, 执行时间: {}ms", methodName, executeTime);
        }
    }

    /**
     * 记录异常日志
     */
    public static void logException(String userId, Exception e) {
        initTraceContext(userId);
        log.error("异常日志 - 用户: {}, 异常: {}", userId, e.getMessage(), e);
        clearTraceContext();
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
            } else {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取User-Agent
     */
    public static String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
