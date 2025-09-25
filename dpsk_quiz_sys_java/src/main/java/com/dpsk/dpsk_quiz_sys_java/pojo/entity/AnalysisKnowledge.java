package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库关联分析表实体类
 */
@Entity
@Table(name = "analysis_knowledge")
public class AnalysisKnowledge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "chunk_id", nullable = false)
    private Long chunkId;
    
    @Column(name = "chapter", length = 255)
    private String chapter;
    
    @Column(name = "question_count", nullable = false)
    private Integer questionCount;
    
    @Column(name = "average_correct_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal averageCorrectRate;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    // 关联知识块实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", insertable = false, updatable = false)
    private KbChunk kbChunk;
    
    // 构造函数
    public AnalysisKnowledge() {
        this.updateTime = LocalDateTime.now();
    }
    
    public AnalysisKnowledge(Long chunkId, String chapter) {
        this();
        this.chunkId = chunkId;
        this.chapter = chapter;
        this.questionCount = 0;
        this.averageCorrectRate = BigDecimal.ZERO;
    }
    
    // 在更新前自动设置更新时间
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChunkId() {
        return chunkId;
    }
    
    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }
    
    public String getChapter() {
        return chapter;
    }
    
    public void setChapter(String chapter) {
        this.chapter = chapter;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public BigDecimal getAverageCorrectRate() {
        return averageCorrectRate;
    }
    
    public void setAverageCorrectRate(BigDecimal averageCorrectRate) {
        this.averageCorrectRate = averageCorrectRate;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public KbChunk getKbChunk() {
        return kbChunk;
    }
    
    public void setKbChunk(KbChunk kbChunk) {
        this.kbChunk = kbChunk;
    }
}