package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisKnowledgeRepository extends JpaRepository<AnalysisKnowledge, Long> {
    
    // 根据知识块ID查找分析
    Optional<AnalysisKnowledge> findByChunkId(Long chunkId);
    
    // 根据题目数量范围查找
    List<AnalysisKnowledge> findByQuestionCountBetween(Integer minCount, Integer maxCount);
    
    // 根据平均正确率范围查找
    List<AnalysisKnowledge> findByAverageCorrectRateBetween(BigDecimal minRate, BigDecimal maxRate);
    
    // 按平均正确率升序排列
    List<AnalysisKnowledge> findAllByOrderByAverageCorrectRateAsc();
    
    // 按平均正确率降序排列
    List<AnalysisKnowledge> findAllByOrderByAverageCorrectRateDesc();
    
    // 按题目数量降序排列
    List<AnalysisKnowledge> findAllByOrderByQuestionCountDesc();
    
    // 查找平均正确率低于指定值的知识点
    List<AnalysisKnowledge> findByAverageCorrectRateLessThan(BigDecimal maxRate);
    
    // 查找平均正确率高于指定值的知识点
    List<AnalysisKnowledge> findByAverageCorrectRateGreaterThan(BigDecimal minRate);
    
    // 查找题目数量大于指定值的知识点
    List<AnalysisKnowledge> findByQuestionCountGreaterThan(Integer minCount);
    
    // 查找题目数量小于指定值的知识点
    List<AnalysisKnowledge> findByQuestionCountLessThan(Integer maxCount);
    
    // 计算所有知识点的平均正确率
    @Query("SELECT AVG(a.averageCorrectRate) FROM AnalysisKnowledge a")
    BigDecimal calculateOverallAverageCorrectRate();
    
    // 计算所有知识点的平均题目数量
    @Query("SELECT AVG(a.questionCount) FROM AnalysisKnowledge a")
    Double calculateAverageQuestionCount();
    
    // 查找正确率最低的知识点
    @Query("SELECT a FROM AnalysisKnowledge a ORDER BY a.averageCorrectRate ASC")
    List<AnalysisKnowledge> findLowestCorrectRateKnowledge();
    
    // 查找正确率最高的知识点
    @Query("SELECT a FROM AnalysisKnowledge a ORDER BY a.averageCorrectRate DESC")
    List<AnalysisKnowledge> findHighestCorrectRateKnowledge();
    
    // 查找题目数量最多的知识点
    @Query("SELECT a FROM AnalysisKnowledge a ORDER BY a.questionCount DESC")
    List<AnalysisKnowledge> findMostQuestionedKnowledge();
    
    // 查找题目数量最少的知识点
    @Query("SELECT a FROM AnalysisKnowledge a WHERE a.questionCount > 0 ORDER BY a.questionCount ASC")
    List<AnalysisKnowledge> findLeastQuestionedKnowledge();
    
    // 统计有题目的知识点数量
    Long countByQuestionCountGreaterThan(Integer minCount);
    
    // 统计正确率低于指定值的知识点数量
    Long countByAverageCorrectRateLessThan(BigDecimal maxRate);
    
    // 统计正确率高于指定值的知识点数量
    Long countByAverageCorrectRateGreaterThan(BigDecimal minRate);
    
    // 查找需要重点关注的知识点（正确率低且题目数量多）
    @Query("SELECT a FROM AnalysisKnowledge a WHERE a.averageCorrectRate < :maxRate AND a.questionCount > :minCount ORDER BY a.averageCorrectRate ASC")
    List<AnalysisKnowledge> findProblematicKnowledge(@Param("maxRate") BigDecimal maxRate, @Param("minCount") Integer minCount);
    
    // 查找掌握良好的知识点（正确率高且题目数量多）
    @Query("SELECT a FROM AnalysisKnowledge a WHERE a.averageCorrectRate > :minRate AND a.questionCount > :minCount ORDER BY a.averageCorrectRate DESC")
    List<AnalysisKnowledge> findWellMasteredKnowledge(@Param("minRate") BigDecimal minRate, @Param("minCount") Integer minCount);
    
    // 查找缺乏练习的知识点（题目数量少）
    @Query("SELECT a FROM AnalysisKnowledge a WHERE a.questionCount < :maxCount ORDER BY a.questionCount ASC")
    List<AnalysisKnowledge> findUnderPracticedKnowledge(@Param("maxCount") Integer maxCount);
    
    // 根据知识块ID删除分析数据
    void deleteByChunkId(Long chunkId);
    
    // 查找正确率分布统计
    @Query("SELECT " +
           "SUM(CASE WHEN a.averageCorrectRate >= 90 THEN 1 ELSE 0 END) as excellent, " +
           "SUM(CASE WHEN a.averageCorrectRate >= 80 AND a.averageCorrectRate < 90 THEN 1 ELSE 0 END) as good, " +
           "SUM(CASE WHEN a.averageCorrectRate >= 70 AND a.averageCorrectRate < 80 THEN 1 ELSE 0 END) as average, " +
           "SUM(CASE WHEN a.averageCorrectRate >= 60 AND a.averageCorrectRate < 70 THEN 1 ELSE 0 END) as poor, " +
           "SUM(CASE WHEN a.averageCorrectRate < 60 THEN 1 ELSE 0 END) as failing " +
           "FROM AnalysisKnowledge a")
    Object[] findCorrectRateDistribution();
    
    // 查找题目数量分布统计
    @Query("SELECT " +
           "SUM(CASE WHEN a.questionCount >= 20 THEN 1 ELSE 0 END) as high, " +
           "SUM(CASE WHEN a.questionCount >= 10 AND a.questionCount < 20 THEN 1 ELSE 0 END) as medium, " +
           "SUM(CASE WHEN a.questionCount >= 5 AND a.questionCount < 10 THEN 1 ELSE 0 END) as low, " +
           "SUM(CASE WHEN a.questionCount > 0 AND a.questionCount < 5 THEN 1 ELSE 0 END) as minimal, " +
           "SUM(CASE WHEN a.questionCount = 0 THEN 1 ELSE 0 END) as none " +
           "FROM AnalysisKnowledge a")
    Object[] findQuestionCountDistribution();
}