// SystemLogRepository.java
package com.example.helper.Repository;

import com.example.helper.Entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    /**
     * 根据业务类型查询日志
     */
    List<SystemLog> findByBusinessTypeOrderByCreatedAtDesc(String businessType);

    /**
     * 根据用户ID查询日志
     */
    Page<SystemLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * 根据时间范围查询日志
     */
    @Query("SELECT sl FROM SystemLog sl WHERE sl.createdAt BETWEEN ?1 AND ?2 ORDER BY sl.createdAt DESC")
    List<SystemLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计错误日志数量
     */
    @Query("SELECT COUNT(sl) FROM SystemLog sl WHERE sl.status = 'FAILED'")
    Long countErrorLogs();

    /**
     * 统计各类业务操作数量
     */
    @Query("SELECT sl.businessType, COUNT(sl) FROM SystemLog sl GROUP BY sl.businessType")
    List<Object[]> countByBusinessType();
}
