package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 试卷表实体类
 */
@Entity
@Table(name = "exam_paper")
public class ExamPaper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Long paperId;
    
    @Column(name = "paper_name", length = 255, nullable = false)
    private String paperName;
    
    @Column(name = "generate_time", nullable = false)
    private LocalDateTime generateTime;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chapter_scope", columnDefinition = "JSON")
    private List<String> chapterScope;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_config", columnDefinition = "JSON")
    private Map<String, Integer> questionConfig;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;
    
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;
    
    @Column(name = "total_question", nullable = false)
    private Integer totalQuestion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaperStatus status = PaperStatus.draft;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    // 难度等级枚举
    public enum Difficulty {
        easy, medium, hard
    }
    
    // 试卷状态枚举
    public enum PaperStatus {
        draft, published, expired
    }
    
    // 构造函数
    public ExamPaper() {
        this.generateTime = LocalDateTime.now();
    }
    
    public ExamPaper(String paperName, Long creatorId, Difficulty difficulty, Integer totalScore, Integer totalQuestion) {
        this();
        this.paperName = paperName;
        this.creatorId = creatorId;
        this.difficulty = difficulty;
        this.totalScore = totalScore;
        this.totalQuestion = totalQuestion;
    }
    
    // Getters and Setters
    public Long getPaperId() {
        return paperId;
    }
    
    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }
    
    public String getPaperName() {
        return paperName;
    }
    
    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
    
    public LocalDateTime getGenerateTime() {
        return generateTime;
    }
    
    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public List<String> getChapterScope() {
        return chapterScope;
    }
    
    public void setChapterScope(List<String> chapterScope) {
        this.chapterScope = chapterScope;
    }
    
    public Map<String, Integer> getQuestionConfig() {
        return questionConfig;
    }
    
    public void setQuestionConfig(Map<String, Integer> questionConfig) {
        this.questionConfig = questionConfig;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    
    public Integer getTotalQuestion() {
        return totalQuestion;
    }
    
    public void setTotalQuestion(Integer totalQuestion) {
        this.totalQuestion = totalQuestion;
    }
    
    public PaperStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaperStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}