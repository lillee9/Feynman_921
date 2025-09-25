package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.AnalysisRecordDto;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisRecord;

import java.util.Optional;

public interface AnalysisRecordService {
    /**
     * 保存分析记录到数据库
     * @param dto 分析记录DTO
     */
    void saveAnalysisRecord(AnalysisRecordDto dto);

    boolean existsByUserAndPaper(Long userId, Long paperId);

    Optional<AnalysisRecord> getByUserAndPaper(Long userId, Long paperId);

    void deleteByUserAndPaper(Long userId, Long paperId);
}