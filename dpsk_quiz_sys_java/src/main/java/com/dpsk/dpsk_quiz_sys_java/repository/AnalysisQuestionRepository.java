package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisQuestionRepository extends JpaRepository<AnalysisQuestion, Long> {
    
    // 根据题目ID查找分析
    Optional<AnalysisQuestion> findByQuestionId(Long questionId);
    
    // 根据试卷ID查找所有题目分析
    List<AnalysisQuestion> findByPaperId(Long paperId);
    
    // 根据章节查找题目分析
    List<AnalysisQuestion> findByChapter(String chapter);
    
    // 根据试卷ID和章节查找
    List<AnalysisQuestion> findByPaperIdAndChapter(Long paperId, String chapter);
    
    // 根据正确率范围查找
    List<AnalysisQuestion> findByCorrectRateBetween(BigDecimal minRate, BigDecimal maxRate);
    
    // 根据答题总数范围查找
    List<AnalysisQuestion> findByTotalAnswerBetween(Integer minAnswers, Integer maxAnswers);
    
    // 根据试卷ID和正确率范围查找
    List<AnalysisQuestion> findByPaperIdAndCorrectRateBetween(Long paperId, BigDecimal minRate, BigDecimal maxRate);
    
    // 根据试卷ID按正确率升序排列
    List<AnalysisQuestion> findByPaperIdOrderByCorrectRateAsc(Long paperId);
    
    // 根据试卷ID按正确率降序排列
    List<AnalysisQuestion> findByPaperIdOrderByCorrectRateDesc(Long paperId);
    
    // 根据试卷ID查找，按答题总数降序排列
    List<AnalysisQuestion> findByPaperIdOrderByTotalAnswerDesc(Long paperId);
    
    // 查找正确率低于指定值的题目
    List<AnalysisQuestion> findByCorrectRateLessThan(BigDecimal maxRate);
    
    // 查找正确率高于指定值的题目
    List<AnalysisQuestion> findByCorrectRateGreaterThan(BigDecimal minRate);
    
    // 查找指定试卷中正确率最低的题目
    @Query("SELECT a FROM AnalysisQuestion a WHERE a.paperId = :paperId ORDER BY a.correctRate ASC")
    List<AnalysisQuestion> findLowestCorrectRateByPaperId(@Param("paperId") Long paperId);
    
    // 查找指定试卷中正确率最高的题目
    @Query("SELECT a FROM AnalysisQuestion a WHERE a.paperId = :paperId ORDER BY a.correctRate DESC")
    List<AnalysisQuestion> findHighestCorrectRateByPaperId(@Param("paperId") Long paperId);
    
    // 计算指定试卷的平均正确率
    @Query("SELECT AVG(a.correctRate) FROM AnalysisQuestion a WHERE a.paperId = :paperId")
    BigDecimal calculateAverageCorrectRateByPaperId(@Param("paperId") Long paperId);
    
    // 计算指定章节的平均正确率
    @Query("SELECT AVG(a.correctRate) FROM AnalysisQuestion a WHERE a.chapter = :chapter")
    BigDecimal calculateAverageCorrectRateByChapter(@Param("chapter") String chapter);
    
    // 统计指定试卷的题目数量
    Long countByPaperId(Long paperId);
    
    // 统计指定章节的题目数量
    Long countByChapter(String chapter);
    
    // 统计正确率低于指定值的题目数量
    Long countByCorrectRateLessThan(BigDecimal maxRate);
    
    // 统计指定试卷中正确率低于指定值的题目数量
    Long countByPaperIdAndCorrectRateLessThan(Long paperId, BigDecimal maxRate);
    
    // 查找指定试卷的章节分布
    @Query("SELECT DISTINCT a.chapter FROM AnalysisQuestion a WHERE a.paperId = :paperId ORDER BY a.chapter")
    List<String> findDistinctChaptersByPaperId(@Param("paperId") Long paperId);
    
    // 查找各章节的题目统计
    @Query("SELECT a.chapter, COUNT(a), AVG(a.correctRate), AVG(a.totalAnswer) FROM AnalysisQuestion a WHERE a.paperId = :paperId GROUP BY a.chapter")
    List<Object[]> findChapterStatisticsByPaperId(@Param("paperId") Long paperId);
    
    // 根据试卷ID删除所有分析数据
    void deleteByPaperId(Long paperId);
    
    // 根据题目ID删除分析数据
    void deleteByQuestionId(Long questionId);
    
    // 查找需要重点关注的题目（正确率低且答题人数多）
    @Query("SELECT a FROM AnalysisQuestion a WHERE a.correctRate < :maxRate AND a.totalAnswer > :minAnswers ORDER BY a.correctRate ASC")
    List<AnalysisQuestion> findProblematicQuestions(@Param("maxRate") BigDecimal maxRate, @Param("minAnswers") Integer minAnswers);
    
    // 查找指定试卷中的难题（正确率低）
    @Query("SELECT a FROM AnalysisQuestion a WHERE a.paperId = :paperId AND a.correctRate < :maxRate ORDER BY a.correctRate ASC")
    List<AnalysisQuestion> findDifficultQuestionsByPaperId(@Param("paperId") Long paperId, @Param("maxRate") BigDecimal maxRate);
}