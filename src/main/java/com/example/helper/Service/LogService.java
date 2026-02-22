// LogService.java
package com.example.helper.Service;

import com.example.helper.Common.Enum.BusinessTypeEnum;
import com.example.helper.Common.Enum.OperationTypeEnum;
import com.example.helper.Common.Util.LoggingUtil;
import com.example.helper.Entity.SystemLog;
import com.example.helper.Repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final SystemLogRepository systemLogRepository;

    /**
     * 异步保存系统日志
     */
    @Async
    public void saveSystemLog(SystemLog systemLog) {
        try {
            systemLogRepository.save(systemLog);
            log.info("系统日志保存成功 - ID: {}", systemLog.getId());
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }

    /**
     * 记录业务操作日志
     */
    public void logBusinessOperation(String userId, BusinessTypeEnum businessType,
                                   OperationTypeEnum operationType, String methodName,
                                   String requestParams, String responseResult,
                                   Long executeTime, String status) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUserId(userId);
            systemLog.setBusinessType(businessType.getCode());
            systemLog.setOperationType(operationType.getCode());
            systemLog.setMethodName(methodName);
            systemLog.setRequestParams(requestParams);
            systemLog.setResponseResult(responseResult);
            systemLog.setExecuteTime(executeTime);
            systemLog.setIpAddress(LoggingUtil.getClientIpAddress());
            systemLog.setUserAgent(LoggingUtil.getUserAgent());
            systemLog.setStatus(status);
            systemLog.setModuleName(businessType.getDescription());

            saveSystemLog(systemLog);

        } catch (Exception e) {
            log.error("记录业务日志失败", e);
        }
    }

    /**
     * 记录异常日志
     */
    public void logException(String userId, String methodName, Exception exception) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUserId(userId);
            systemLog.setMethodName(methodName);
            systemLog.setExceptionInfo(exception.getMessage());
            systemLog.setIpAddress(LoggingUtil.getClientIpAddress());
            systemLog.setStatus("FAILED");
            systemLog.setModuleName("系统异常");

            saveSystemLog(systemLog);

        } catch (Exception e) {
            log.error("记录异常日志失败", e);
        }
    }

    /**
     * 分页查询用户日志
     */
    public Page<SystemLog> getUserLogs(String userId, Pageable pageable) {
        return systemLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 根据业务类型查询日志
     */
    public List<SystemLog> getLogsByBusinessType(String businessType) {
        return systemLogRepository.findByBusinessTypeOrderByCreatedAtDesc(businessType);
    }

    /**
     * 查询错误日志统计
     */
    public Long getErrorLogCount() {
        return systemLogRepository.countErrorLogs();
    }

    /**
     * 获取业务类型统计
     */
    public List<Object[]> getBusinessTypeStatistics() {
        return systemLogRepository.countByBusinessType();
    }
}
