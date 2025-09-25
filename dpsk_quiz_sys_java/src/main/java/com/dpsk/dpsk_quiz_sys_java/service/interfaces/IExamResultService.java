package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamResult;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 考试结果服务接口
 * 提供考试结果管理、成绩统计和分析功能
 */
public interface IExamResultService {
    
    /**
     * 提交考试答案并计算成绩
     * @param submitRequest 答案提交请求
     * @return 考试结果
     */
    ExamResultResponse submitExamAnswers(ExamAnswerSubmitRequest submitRequest);
    
    /**
     * 获取考试结果详情
     * @param resultId 结果ID
     * @return 考试结果详情
     */
    ExamResultResponse getExamResult(Long resultId);
    
    /**
     * 获取用户的考试结果列表
     * @param userId 用户ID
     * @param pageRequest 分页请求
     * @return 分页的考试结果列表
     */
    PageResponse<ExamResultResponse> getUserExamResults(Long userId, PageRequest pageRequest);
    
    /**
     * 获取试卷的所有考试结果
     * @param paperId 试卷ID
     * @param pageRequest 分页请求
     * @return 分页的考试结果列表
     */
    PageResponse<ExamResultResponse> getPaperExamResults(Long paperId, PageRequest pageRequest);
    
    /**
     * 获取用户在指定试卷的考试结果
     * @param userId 用户ID
     * @param paperId 试卷ID
     * @return 考试结果，如果不存在返回null
     */
    ExamResultResponse getUserPaperResult(Long userId, Long paperId);
    
    /**
     * 删除考试结果
     * @param resultId 结果ID
     */
    void deleteExamResult(Long resultId);
    
    /**
     * 批量删除考试结果
     * @param resultIds 结果ID列表
     */
    void deleteExamResults(List<Long> resultIds);
    
    /**
     * 获取试卷成绩统计
     * @param paperId 试卷ID
     * @return 成绩统计数据
     */
    Map<String, Object> getPaperStatistics(Long paperId);
    
    /**
     * 获取用户成绩统计
     * @param userId 用户ID
     * @return 用户成绩统计数据
     */
    Map<String, Object> getUserStatistics(Long userId);
    
    /**
     * 获取试卷排行榜
     * @param paperId 试卷ID
     * @param limit 排行榜数量限制
     * @return 排行榜列表
     */
    List<ExamResultResponse> getPaperRanking(Long paperId, Integer limit);
    
    /**
     * 计算试卷及格率
     * @param paperId 试卷ID
     * @param passScore 及格分数
     * @return 及格率（百分比）
     */
    Double calculatePassRate(Long paperId, BigDecimal passScore);
    
    /**
     * 获取分数分布统计
     * @param paperId 试卷ID
     * @return 分数分布数据
     */
    Map<String, Object> getScoreDistribution(Long paperId);
    
    /**
     * 获取答题分析数据
     * @param paperId 试卷ID
     * @return 答题分析数据
     */
    AnalysisResponse getAnswerAnalysis(Long paperId);
    
    /**
     * 重新计算考试成绩（用于题目分数调整后）
     * @param resultId 结果ID
     * @return 重新计算后的考试结果
     */
    ExamResultResponse recalculateScore(Long resultId);
    
    /**
     * 检查用户是否已参加指定试卷考试
     * @param userId 用户ID
     * @param paperId 试卷ID
     * @return 是否已参加
     */
    boolean hasUserTakenExam(Long userId, Long paperId);
    
    /**
     * 获取考试结果导出数据
     * @param paperId 试卷ID
     * @return 导出数据列表
     */
    List<Map<String, Object>> getExportData(Long paperId);

    Optional<ExamResult> getByUserAndPaper(Long userId, Long paperId);

    List<ExamResult> getByUserId(Long userId);

    List<ExamResult> getByUserId_Mapper(Long userId);

    Map<QuestionType, String> getQuestionTypeAccuracy(Long userId, Long paperId);

    Double calculatePassRate_ljs(Long paperId, BigDecimal passScore);
}