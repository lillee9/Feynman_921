package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 题目分析表实体类
 */
@Entity
@Table(name = "analysis_question")
public class AnalysisQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    
    @Column(name = "total_answer", nullable = false)
    private Integer totalAnswer;
    
    @Column(name = "correct_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal correctRate;
    
    @Column(name = "chapter", length = 255)
    private String chapter;
    
    // 关联试题实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private ExamQuestion examQuestion;
    
    // 关联试卷实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private ExamPaper examPaper;
    
    // 构造函数
    public AnalysisQuestion() {}
    
    public AnalysisQuestion(Long questionId, Long paperId, String chapter) {
        this.questionId = questionId;
        this.paperId = paperId;
        this.chapter = chapter;
        this.totalAnswer = 0;
        this.correctRate = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public Long getPaperId() {
        return paperId;
    }
    
    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }
    
    public Integer getTotalAnswer() {
        return totalAnswer;
    }
    
    public void setTotalAnswer(Integer totalAnswer) {
        this.totalAnswer = totalAnswer;
    }
    
    public BigDecimal getCorrectRate() {
        return correctRate;
    }
    
    public void setCorrectRate(BigDecimal correctRate) {
        this.correctRate = correctRate;
    }
    
    public String getChapter() {
        return chapter;
    }
    
    public void setChapter(String chapter) {
        this.chapter = chapter;
    }
    
    public ExamQuestion getExamQuestion() {
        return examQuestion;
    }
    
    public void setExamQuestion(ExamQuestion examQuestion) {
        this.examQuestion = examQuestion;
    }
    
    public ExamPaper getExamPaper() {
        return examPaper;
    }
    
    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }
}