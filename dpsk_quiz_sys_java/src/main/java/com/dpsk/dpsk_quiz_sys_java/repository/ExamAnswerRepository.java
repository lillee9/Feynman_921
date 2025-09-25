package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    
    // 根据用户ID和试卷ID查找答案
    List<ExamAnswer> findByUserIdAndPaperId(Long userId, Long paperId);
    
    // 根据用户ID查找所有答案
    List<ExamAnswer> findByUserId(Long userId);
    
    // 根据试卷ID查找所有答案
    List<ExamAnswer> findByPaperId(Long paperId);
    
    // 根据题目ID查找所有答案
    List<ExamAnswer> findByQuestionId(Long questionId);
     ExamAnswer findByUserIdAndQuestionId(Long userId, Long questionId);
    // 根据用户ID和题目ID查找特定答案
//    Optional<ExamAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

    // 根据用户ID、试卷ID和题目ID查找答案
    Optional<ExamAnswer> findByUserIdAndPaperIdAndQuestionId(Long userId, Long paperId, Long questionId);
    
    // 根据正确性查找答案
    List<ExamAnswer> findByIsCorrect(Boolean isCorrect);
    
    // 根据用户ID和正确性查找答案
    List<ExamAnswer> findByUserIdAndIsCorrect(Long userId, Boolean isCorrect);
    
    // 根据试卷ID和正确性查找答案
    List<ExamAnswer> findByPaperIdAndIsCorrect(Long paperId, Boolean isCorrect);
    
    // 根据答题时间范围查找
    List<ExamAnswer> findByAnswerTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据用户ID和答题时间范围查找
    List<ExamAnswer> findByUserIdAndAnswerTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 统计指定用户的答题数量
    Long countByUserId(Long userId);
    
    // 统计指定试卷的答题数量
    Long countByPaperId(Long paperId);
    
    // 统计指定题目的答题数量
    Long countByQuestionId(Long questionId);
    
    // 统计指定用户在指定试卷的答题数量
    Long countByUserIdAndPaperId(Long userId, Long paperId);
    
    // 统计指定用户的正确答题数量
    Long countByUserIdAndIsCorrect(Long userId, Boolean isCorrect);
    
    // 统计指定试卷的正确答题数量
    Long countByPaperIdAndIsCorrect(Long paperId, Boolean isCorrect);
    
    // 统计指定题目的正确答题数量
    Long countByQuestionIdAndIsCorrect(Long questionId, Boolean isCorrect);
    
    // 计算指定题目的正确率
    @Query("SELECT (COUNT(CASE WHEN a.isCorrect = true THEN 1 END) * 100.0 / COUNT(a)) FROM ExamAnswer a WHERE a.questionId = :questionId")
    Double calculateCorrectRateByQuestionId(@Param("questionId") Long questionId);
    
    // 计算指定用户在指定试卷的正确率
    @Query("SELECT (COUNT(CASE WHEN a.isCorrect = true THEN 1 END) * 100.0 / COUNT(a)) FROM ExamAnswer a WHERE a.userId = :userId AND a.paperId = :paperId")
    Double calculateCorrectRateByUserIdAndPaperId(@Param("userId") Long userId, @Param("paperId") Long paperId);
    
    // 查找指定用户最近的答题记录
    @Query("SELECT a FROM ExamAnswer a WHERE a.userId = :userId ORDER BY a.answerTime DESC")
    List<ExamAnswer> findRecentAnswersByUserId(@Param("userId") Long userId);
    
    // 根据用户ID和试卷ID删除答案
    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAnswer a WHERE a.userId = :userId AND a.paperId = :paperId")
    void deleteByUserIdAndPaperId(@Param("userId") Long userId, @Param("paperId") Long paperId);
    
    // 根据试卷ID删除所有答案
    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAnswer a WHERE a.paperId = :paperId")
    void deleteByPaperId(@Param("paperId") Long paperId);
    
    // 根据题目ID删除所有答案
    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAnswer a WHERE a.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
    
    // 查找指定试卷中答题最多的用户
    @Query("SELECT a.userId, COUNT(a) as answerCount FROM ExamAnswer a WHERE a.paperId = :paperId GROUP BY a.userId ORDER BY answerCount DESC")
    List<Object[]> findTopAnswerersByPaperId(@Param("paperId") Long paperId);
    
    // 查找指定时间段内的活跃用户
    @Query("SELECT DISTINCT a.userId FROM ExamAnswer a WHERE a.answerTime BETWEEN :startTime AND :endTime")
    List<Integer> findActiveUsersBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}