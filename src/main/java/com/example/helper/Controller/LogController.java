// LogController.java
package com.example.helper.Controller;

import com.example.helper.Common.Util.ApiResponse;
import com.example.helper.Entity.SystemLog;
import com.example.helper.Service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "系统日志相关接口")
public class LogController {

    private final LogService logService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户日志", description = "分页查询指定用户的操作日志")
    public ApiResponse<Page<SystemLog>> getUserLogs(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SystemLog> logs = logService.getUserLogs(userId, pageable);
        return ApiResponse.success("查询成功", logs);
    }

    @GetMapping("/business/{businessType}")
    @Operation(summary = "查询业务日志", description = "根据业务类型查询相关日志")
    public ApiResponse<List<SystemLog>> getBusinessLogs(
            @Parameter(description = "业务类型") @PathVariable String businessType) {

        List<SystemLog> logs = logService.getLogsByBusinessType(businessType);
        return ApiResponse.success("查询成功", logs);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取日志统计", description = "获取系统日志统计信息")
    public ApiResponse<Map<String, Object>> getLogStatistics() {

        Long errorCount = logService.getErrorLogCount();
        List<Object[]> businessStats = logService.getBusinessTypeStatistics();

        Map<String, Long> businessTypeStats = businessStats.stream()
                .collect(Collectors.toMap(
                        stat -> (String) stat[0],
                        stat -> (Long) stat[1]
                ));

        Map<String, Object> statistics = Map.of(
                "errorCount", errorCount,
                "businessTypeStats", businessTypeStats
        );

        return ApiResponse.success("统计成功", statistics);
    }
}
