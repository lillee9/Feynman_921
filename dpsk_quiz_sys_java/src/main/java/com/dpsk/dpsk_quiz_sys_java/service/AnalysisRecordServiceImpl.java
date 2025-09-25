package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.AnalysisRecordDto;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisRecord;
import com.dpsk.dpsk_quiz_sys_java.repository.AnalysisRecordRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class AnalysisRecordServiceImpl implements AnalysisRecordService {

    @Resource
    private AnalysisRecordRepository analysisRecordRepository;  // 注入JPA Repository

    @Override
    public void saveAnalysisRecord(AnalysisRecordDto dto) {
        AnalysisRecord record = new AnalysisRecord();
        BeanUtils.copyProperties(dto, record);  // DTO转实体（需lombok或手动setter）
        analysisRecordRepository.save(record);  // JPA自动生成保存逻辑
    }
    @Override
    public boolean existsByUserAndPaper(Long userId, Long paperId) {
        return analysisRecordRepository.existsByUserIdAndPaperId(userId, paperId);
    }

    // 新增：Controller需要的查询方法（如果之前未定义）
    @Override
    public Optional<AnalysisRecord> getByUserAndPaper(Long userId, Long paperId) {
        return analysisRecordRepository.findByUserIdAndPaperId(userId, paperId);
    }

    @Override
    @Transactional
    public void deleteByUserAndPaper(Long userId, Long paperId) {
        analysisRecordRepository.deleteByUserIdAndPaperId(userId, paperId);
    }

}