package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    // 根据试卷ID查找所有题目
    List<ExamQuestion> findByPaperId(Long paperId);
    
    // 根据试卷ID和题目类型查找
    List<ExamQuestion> findByPaperIdAndQuestionType(Long paperId, QuestionType questionType);
    
    // 根据试卷ID按排序顺序查找
    List<ExamQuestion> findByPaperIdOrderBySort(Long paperId);
    
    // 根据知识块ID查找题目
    List<ExamQuestion> findByChunkId(Long chunkId);
    
    // 根据题目类型查找
    List<ExamQuestion> findByQuestionType(QuestionType questionType);
    
    // 根据分数范围查找
    List<ExamQuestion> findByScoreBetween(BigDecimal minScore, BigDecimal maxScore);
    
    // 根据试卷ID和分数范围查找
    List<ExamQuestion> findByPaperIdAndScoreBetween(Long paperId, BigDecimal minScore, BigDecimal maxScore);
    
    // 根据题目内容模糊查询
    @Query("SELECT q FROM ExamQuestion q WHERE q.questionContent LIKE %:keyword%")
    List<ExamQuestion> findByQuestionContentContaining(@Param("keyword") String keyword);
    
    // 根据试卷ID和题目内容模糊查询
    @Query("SELECT q FROM ExamQuestion q WHERE q.paperId = :paperId AND q.questionContent LIKE %:keyword%")
    List<ExamQuestion> findByPaperIdAndQuestionContentContaining(@Param("paperId") Long paperId, @Param("keyword") String keyword);
    
    // 统计指定试卷的题目数量
    Long countByPaperId(Long paperId);
    
    // 统计指定试卷和题目类型的数量
    Long countByPaperIdAndQuestionType(Long paperId, QuestionType questionType);
    
    // 计算指定试卷的总分
    @Query("SELECT SUM(q.score) FROM ExamQuestion q WHERE q.paperId = :paperId")
    BigDecimal calculateTotalScoreByPaperId(@Param("paperId") Long paperId);
    
    // 获取指定试卷的最大排序值
    @Query("SELECT MAX(q.sort) FROM ExamQuestion q WHERE q.paperId = :paperId")
    Integer findMaxSortByPaperId(@Param("paperId") Long paperId);
    
    // 根据试卷ID删除所有题目
    void deleteByPaperId(Long paperId);
    
    // 根据知识块ID删除题目
    void deleteByChunkId(Long chunkId);
    
    // 查找指定试卷的题目类型分布
    @Query("SELECT q.questionType, COUNT(q) FROM ExamQuestion q WHERE q.paperId = :paperId GROUP BY q.questionType")
    List<Object[]> findQuestionTypeDistributionByPaperId(@Param("paperId") Long paperId);
    
    // 查找指定知识块的题目数量
    Long countByChunkId(Long chunkId);
    
    // 根据试卷ID和排序范围查找
    List<ExamQuestion> findByPaperIdAndSortBetween(Long paperId, Integer startOrder, Integer endOrder);
    
    // 查找指定试卷中包含分析的题目
    @Query("SELECT q FROM ExamQuestion q WHERE q.paperId = :paperId AND q.analysis IS NOT NULL AND q.analysis != ''")
    List<ExamQuestion> findQuestionsWithAnalysisByPaperId(@Param("paperId") Long paperId);

    @Query("SELECT q FROM ExamQuestion q " +
            "LEFT JOIN FETCH q.examPaper " +
            "LEFT JOIN FETCH q.kbChunk " +
            "WHERE q.paperId = :paperId")
    List<ExamQuestion> findByPaperIdWithFetch(@Param("paperId") Long paperId);
}