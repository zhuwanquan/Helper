// SystemLog.java
package com.example.helper.Entity;

import com.example.helper.Common.Base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "system_logs")
@EqualsAndHashCode(callSuper = true)
public class SystemLog extends BaseEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "business_type", length = 20)
    private String businessType;

    @Column(name = "operation_type", length = 20)
    private String operationType;

    @Column(name = "method_name", length = 200)
    private String methodName;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "response_result", columnDefinition = "TEXT")
    private String responseResult;

    @Column(name = "execute_time")
    private Long executeTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "exception_info", columnDefinition = "TEXT")
    private String exceptionInfo;

    @Column(name = "status", length = 10)
    private String status; // SUCCESS, FAILED

    @Column(name = "module_name", length = 100)
    private String moduleName;
}
