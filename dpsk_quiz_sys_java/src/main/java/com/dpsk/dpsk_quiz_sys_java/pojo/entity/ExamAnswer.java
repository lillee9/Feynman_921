package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题记录表实体类
 */
@Data
@Entity
@Table(name = "exam_answer")
public class ExamAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_answer", columnDefinition = "JSON")
    private List<String> userAnswer;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column(name = "answer_time")
    private LocalDateTime answerTime;
    
    // 关联试卷实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private ExamPaper examPaper;
    
    // 关联试题实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private ExamQuestion examQuestion;
    
    // 构造函数
    public ExamAnswer() {}
    
    public ExamAnswer(Long userId, Long paperId, Long questionId, List<String> userAnswer) {
        this.userId = userId;
        this.paperId = paperId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.answerTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getAnswerId() {
        return answerId;
    }
    
    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
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
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public List<String> getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(List<String> userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public LocalDateTime getAnswerTime() {
        return answerTime;
    }
    
    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }
    
    public ExamPaper getExamPaper() {
        return examPaper;
    }
    
    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }
    
    public ExamQuestion getExamQuestion() {
        return examQuestion;
    }
    
    public void setExamQuestion(ExamQuestion examQuestion) {
        this.examQuestion = examQuestion;
    }
}