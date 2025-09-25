package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JPA自动生成CRUD实现，无需手动编写Mapper
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Integer> {
    // 新增1：检查是否存在指定用户和试卷的分析记录（JPA自动解析方法名生成查询）
    boolean existsByUserIdAndPaperId(Long userId, Long paperId);

    // 新增2：根据用户ID和试卷ID查询分析记录（返回Optional避免空指针）
    Optional<AnalysisRecord> findByUserIdAndPaperId(Long userId, Long paperId);

    void deleteByUserIdAndPaperId(Long userId, Long paperId);
}