// LoggingAspect.java
package com.example.helper.Common.Aspect;

import com.example.helper.Common.Util.LoggingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 环绕通知 - 记录Service层方法执行
     */
    @Around("execution(* com.example.helper.Service..*.*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        try {
            // 记录方法入口
            log.debug("进入方法: {}.{}", className, methodName);

            // 执行原方法
            Object result = joinPoint.proceed();

            // 记录方法出口
            long executeTime = System.currentTimeMillis() - startTime;
            log.debug("退出方法: {}.{}, 执行时间: {}ms", className, methodName, executeTime);

            // 记录性能日志
            LoggingUtil.logPerformance(className + "." + methodName, executeTime);

            return result;

        } catch (Exception e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("方法执行异常: {}.{} 执行时间: {}ms 异常: {}",
                     className, methodName, executeTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 环绕通知 - 记录Controller层方法执行
     */
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        try {
            // 记录请求参数
            Object[] args = joinPoint.getArgs();
            String params = serializeParams(args);
            log.info("Controller请求 - 方法: {}.{} 参数: {}", className, methodName, params);

            // 执行原方法
            Object result = joinPoint.proceed();

            // 记录响应结果
            long executeTime = System.currentTimeMillis() - startTime;
            log.info("Controller响应 - 方法: {}.{}, 执行时间: {}ms",
                    className, methodName, executeTime);

            return result;

        } catch (Exception e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("Controller异常 - 方法: {}.{} 执行时间: {}ms 异常: {}",
                     className, methodName, executeTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 序列化参数
     */
    private String serializeParams(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "{}";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");

            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    sb.append("\"arg").append(i).append("\":")
                      .append(objectMapper.writeValueAsString(args[i]));
                    if (i < args.length - 1) {
                        sb.append(",");
                    }
                }
            }

            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }
}
