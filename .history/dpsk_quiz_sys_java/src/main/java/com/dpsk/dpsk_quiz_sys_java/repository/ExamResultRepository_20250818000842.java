package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    // 根据用户ID和试卷ID查找结果
    Optional<ExamResult> findByUserIdAndPaperId(Long userId, Long paperId);
    // 根据用户ID查找所有考试结果
    List<ExamResult> findByUserId(Long userId);

    // 根据试卷ID查找所有考试结果
    List<ExamResult> findByPaperId(Long paperId);

    // 根据分数范围查找
    List<ExamResult> findByTotalScoreBetween(BigDecimal minScore, BigDecimal maxScore);

    // 根据用户ID和分数范围查找
    List<ExamResult> findByUserIdAndTotalScoreBetween(Long userId, BigDecimal minScore, BigDecimal maxScore);

    // 根据试卷ID和分数范围查找
    List<ExamResult> findByPaperIdAndTotalScoreBetween(Long paperId, BigDecimal minScore, BigDecimal maxScore);

    // 根据考试开始时间范围查找
    List<ExamResult> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据考试结束时间范围查找
    List<ExamResult> findByEndTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据考试时长范围查找
    List<ExamResult> findByDurationBetween(Integer minDuration, Integer maxDuration);

    // 根据排名范围查找
    List<ExamResult> findByRankBetween(Integer minRank, Integer maxRank);

    // 根据试卷ID按分数降序排列
    List<ExamResult> findByPaperIdOrderByTotalScoreDesc(Long paperId);

    // 根据试卷ID按考试时间降序排列
    List<ExamResult> findByPaperIdOrderByStartTimeDesc(Long paperId);

    // 根据用户ID按考试时间降序排列
    List<ExamResult> findByUserIdOrderByStartTimeDesc(Long userId);

    // 统计指定用户的考试次数
    Long countByUserId(Long userId);

    // 统计指定试卷的参考人数
    Long countByPaperId(Long paperId);

    // 计算指定试卷的平均分
    @Query("SELECT AVG(r.totalScore) FROM ExamResult r WHERE r.paperId = :paperId")
    BigDecimal calculateAverageScoreByPaperId(@Param("paperId") Long paperId);

    // 计算指定用户的平均分
    @Query("SELECT AVG(r.totalScore) FROM ExamResult r WHERE r.userId = :userId")
    BigDecimal calculateAverageScoreByUserId(@Param("userId") Long userId);

    // 查找指定试卷的最高分
    @Query("SELECT MAX(r.totalScore) FROM ExamResult r WHERE r.paperId = :paperId")
    BigDecimal findMaxScoreByPaperId(@Param("paperId") Long paperId);

    // 查找指定试卷的最低分
    @Query("SELECT MIN(r.totalScore) FROM ExamResult r WHERE r.paperId = :paperId")
    BigDecimal findMinScoreByPaperId(@Param("paperId") Long paperId);

    // 查找指定用户的最高分
    @Query("SELECT MAX(r.totalScore) FROM ExamResult r WHERE r.userId = :userId")
    BigDecimal findMaxScoreByUserId(@Param("userId") Long userId);

    // 查找指定试卷的前N名
    @Query("SELECT r FROM ExamResult r WHERE r.paperId = :paperId ORDER BY r.totalScore DESC, r.duration ASC")
    List<ExamResult> findTopResultsByPaperId(@Param("paperId") Long paperId);

    // 查找指定分数以上的考试结果
    List<ExamResult> findByTotalScoreGreaterThanEqual(BigDecimal minScore);

    // 查找指定试卷中分数以上的结果
    List<ExamResult> findByPaperIdAndTotalScoreGreaterThanEqual(Long paperId, BigDecimal minScore);

    // 统计指定试卷的及格人数（假设60分及格）
    @Query("SELECT COUNT(r) FROM ExamResult r WHERE r.paperId = :paperId AND r.totalScore >= :passScore")
    Long countPassedByPaperId(@Param("paperId") Long paperId, @Param("passScore") BigDecimal passScore);

    // 计算指定试卷的及格率
    @Query("SELECT (SUM(CASE WHEN r.totalScore >= :passScore THEN 1 ELSE 0 END) * 100.0 / COUNT(r)) FROM ExamResult r WHERE r.paperId = :paperId")
    Double calculatePassRateByPaperId_zht(@Param("paperId") Long paperId, @Param("passScore") BigDecimal passScore);

 // 计算指定试卷的及格率
    @Query("SELECT (COUNT(CASE WHEN r.totalScore >= :passScore THEN 1 END) * 100.0 / COUNT(r)) FROM ExamResult r WHERE r.paperId = :paperId")
    Double calculatePassRateByPaperId(@Param("paperId") Long paperId, @Param("passScore") BigDecimal passScore);
    
    // 根据试卷ID删除所有结果
    void deleteByPaperId(Long paperId);

    // 根据用户ID删除所有结果
    void deleteByUserId(Long userId);

    // 查找最近的考试结果
    @Query("SELECT r FROM ExamResult r ORDER BY r.startTime DESC")
    List<ExamResult> findRecentResults();

    // 查找指定时间段内的考试结果
    @Query("SELECT r FROM ExamResult r WHERE r.startTime BETWEEN :startTime AND :endTime ORDER BY r.startTime DESC")
    List<ExamResult> findResultsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<ExamResult> findByPaperDifficulty(ExamPaper.Difficulty paperDifficulty, Pageable pageable);

    /**
     * 统计去重用户数（总参与人数）
     */
    @Query("SELECT COUNT(DISTINCT er.userId) FROM ExamResult er")
    int countDistinctUsers();

    /**
     * 获取各难度的分数段统计（优秀/良好/及格/不及格）
     * 使用原生SQL实现复杂统计（JPQL对CASE WHEN支持有限）
     */
    @Query(value = "SELECT " +
            "paper_difficulty AS difficulty, " +  // 使用实际列名并设置别名为difficulty（匹配ChartService中的row.get("difficulty")）
            "SUM(CASE WHEN total_score >= 90 THEN 1 ELSE 0 END) AS excellent, " +  // 分数字段为total_score（建表语句中字段）
            "SUM(CASE WHEN total_score BETWEEN 75 AND 89 THEN 1 ELSE 0 END) AS good, " +
            "SUM(CASE WHEN total_score BETWEEN 60 AND 74 THEN 1 ELSE 0 END) AS pass, " +
            "SUM(CASE WHEN total_score < 60 THEN 1 ELSE 0 END) AS fail, " +
            "COUNT(*) AS total " +  // 总记录数（当前难度下的考试次数）
            "FROM exam_result " +
            "GROUP BY paper_difficulty",  // 按实际难度列分组
            nativeQuery = true)
    List<Map<String, Object>> getScoreDistribution();

    @Query(value = "SELECT * FROM exam_result WHERE user_id = :userId", nativeQuery = true)
    List<ExamResult> getByUserId_Mapper(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM ExamResult e WHERE e.endTime BETWEEN :start AND :end")
    Integer countUsersByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE_FORMAT(end_time, '%m-%d') AS date, COUNT(DISTINCT user_id) AS count "
            + "FROM exam_result "
            + "WHERE end_time BETWEEN :start AND :end "
            + "GROUP BY date", nativeQuery = true)
    List<Map<String, Object>> findDailyParticipants(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(nativeQuery = true, value = """
    SELECT 
        ep.difficulty,
        eq.question_type,
        SUM(CASE WHEN ea.is_correct THEN 1 ELSE 0 END) AS correct_count,
        COUNT(*) AS total_count
    FROM exam_answer ea
    JOIN exam_question eq ON ea.question_id = eq.question_id
    JOIN exam_paper ep ON eq.paper_id = ep.paper_id
    WHERE ea.answer_time BETWEEN :start AND :end
    GROUP BY ep.difficulty, eq.question_type
    """)
    List<Map<String, Object>> findAccuracyStats(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}