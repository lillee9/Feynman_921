package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    // 直接复用 JpaRepository 提供的 findById 方法，无需额外定义
    // 根据试卷名称查找
    Optional<ExamPaper> findByPaperName(String paperName);
    
    // 根据创建用户查找
    List<ExamPaper> findByCreatorId(Long creatorId);
    
    // 根据创建用户查找（分页）
    Page<ExamPaper> findByCreatorId(Long creatorId, Pageable pageable);
    
    // 根据试卷状态查找
    List<ExamPaper> findByStatus(ExamPaper.PaperStatus status);
    
    // 根据试卷状态查找（分页）
    Page<ExamPaper> findByStatus(ExamPaper.PaperStatus status, Pageable pageable);
    
    // 根据试卷状态和过期时间查找（分页）
    Page<ExamPaper> findByStatusAndExpireTimeAfter(ExamPaper.PaperStatus status, LocalDateTime expireTime, Pageable pageable);
    
    // 根据难度等级查找
    List<ExamPaper> findByDifficulty(ExamPaper.Difficulty difficulty);
    
    // 根据创建用户和状态查找
    List<ExamPaper> findByCreatorIdAndStatus(Long creatorId, ExamPaper.PaperStatus status);
    
    // 根据生成时间范围查找
    List<ExamPaper> findByGenerateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据总分范围查找
    List<ExamPaper> findByTotalScoreBetween(Integer minScore, Integer maxScore);
    
    // 根据总题数范围查找
    List<ExamPaper> findByTotalQuestionBetween(Integer minQuestions, Integer maxQuestions);
    
    // 根据试卷名称模糊查询
    @Query("SELECT p FROM ExamPaper p WHERE p.paperName LIKE %:keyword%")
    List<ExamPaper> findByPaperNameContaining(@Param("keyword") String keyword);
    
    // 统计指定用户创建的试卷数量
    Long countByCreatorId(Long creatorId);
    
    // 统计指定状态的试卷数量
    Long countByStatus(ExamPaper.PaperStatus status);
    
    // 获取最近创建的试卷
    @Query("SELECT p FROM ExamPaper p ORDER BY p.generateTime DESC")
    List<ExamPaper> findRecentPapers();
    
    // 获取已发布的试卷
    @Query("SELECT p FROM ExamPaper p WHERE p.status = 'published' ORDER BY p.generateTime DESC")
    List<ExamPaper> findPublishedPapers();
    
    // 根据难度和状态查找
    List<ExamPaper> findByDifficultyAndStatus(ExamPaper.Difficulty difficulty, ExamPaper.PaperStatus status);
    
    // 获取用户可用的试卷（排除自己创建的）
    @Query("SELECT p FROM ExamPaper p WHERE p.status = 'published' AND p.creatorId != :userId")
    List<ExamPaper> findAvailablePapersForUser(@Param("userId") Long userId);
    
    // 根据题目数量范围查找（使用questionConfig JSON字段）
    @Query(value = "SELECT * FROM exam_papers p WHERE JSON_EXTRACT(p.question_config, '$.totalQuestions') BETWEEN :minQuestions AND :maxQuestions", nativeQuery = true)
    List<ExamPaper> findByQuestionCountBetween(@Param("minQuestions") Integer minQuestions, @Param("maxQuestions") Integer maxQuestions);
    
    // 根据章节范围查找
    @Query(value = "SELECT * FROM exam_papers p WHERE JSON_CONTAINS(p.chapter_scope, JSON_QUOTE(:chapter))", nativeQuery = true)
    List<ExamPaper> findByChapterScope(@Param("chapter") String chapter);

    @Query(value = "SELECT * FROM exam_paper WHERE paper_id = :paperId", nativeQuery = true)
    ExamPaper getById_Mapper(@Param("paperId") Long paperId);

}