// LoggingInterceptor.java
package com.example.helper.Config.Interceptor;

import com.example.helper.Common.Util.LoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 记录请求开始时间
        request.setAttribute("startTime", System.currentTimeMillis());

        // 初始化日志上下文
        String userId = request.getHeader("User-Id"); // 从请求头获取用户ID
        LoggingUtil.initTraceContext(userId);

        log.info("请求开始 - URL: {}, Method: {}, IP: {}",
                request.getRequestURL(),
                request.getMethod(),
                LoggingUtil.getClientIpAddress());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long executeTime = System.currentTimeMillis() - startTime;

        log.info("请求结束 - URL: {}, Status: {}, 执行时间: {}ms",
                request.getRequestURL(),
                response.getStatus(),
                executeTime);

        // 记录性能日志
        LoggingUtil.logPerformance(request.getRequestURI(), executeTime);

        // 清理日志上下文
        LoggingUtil.clearTraceContext();
    }
}
