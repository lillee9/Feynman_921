package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试结果表实体类
 */
@Entity
@Table(name = "exam_result")
public class ExamResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "paper_difficulty", nullable = false)
    private ExamPaper.Difficulty paperDifficulty;
    
    @Column(name = "total_score", precision = 5, scale = 1, nullable = false)
    private BigDecimal totalScore;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration")
    private Integer duration; // 考试时长（分钟）
    
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;
    
    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;
    
    @Column(name = "unanswered_count", nullable = false)
    private Integer unansweredCount;
    
    @Column(name = "`rank`")
    private Integer rank;
    
    // 关联试卷实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private ExamPaper examPaper;
    
    // 构造函数
    public ExamResult() {}
    
    public ExamResult(Long userId, Long paperId, ExamPaper.Difficulty paperDifficulty, 
                     LocalDateTime startTime) {
        this.userId = userId;
        this.paperId = paperId;
        this.paperDifficulty = paperDifficulty;
        this.startTime = startTime;
        this.correctCount = 0;
        this.wrongCount = 0;
        this.unansweredCount = 0;
        this.totalScore = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getResultId() {
        return resultId;
    }
    
    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getPaperId() {
        return paperId;
    }
    
    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }
    
    public ExamPaper.Difficulty getPaperDifficulty() {
        return paperDifficulty;
    }
    
    public void setPaperDifficulty(ExamPaper.Difficulty paperDifficulty) {
        this.paperDifficulty = paperDifficulty;
    }
    
    public BigDecimal getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getCorrectCount() {
        return correctCount;
    }
    
    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }
    
    public Integer getWrongCount() {
        return wrongCount;
    }
    
    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }
    
    public Integer getUnansweredCount() {
        return unansweredCount;
    }
    
    public void setUnansweredCount(Integer unansweredCount) {
        this.unansweredCount = unansweredCount;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public ExamPaper getExamPaper() {
        return examPaper;
    }
    
    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }
}