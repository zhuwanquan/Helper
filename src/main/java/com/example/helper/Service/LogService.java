// LogService.java
package com.example.helper.Service;

import com.example.helper.Entity.SystemLog;
import com.example.helper.Repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final SystemLogRepository systemLogRepository;

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
