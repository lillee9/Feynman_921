package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * 试题表实体类
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exam_question")
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "question_content", columnDefinition = "TEXT", nullable = false)
    private String questionContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "JSON")
    private List<String> options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "correct_answer", columnDefinition = "JSON", nullable = false)
    private List<String> correctAnswer;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;

    @Column(name = "chunk_id")
    private Long chunkId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    // 关联试卷实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private ExamPaper examPaper;

    // 关联知识块实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", insertable = false, updatable = false)
    private KbChunk kbChunk;

    // 题型枚举
    public enum QuestionType {
        single("单选题"),
        multi("多选题"),
        judge("判断题");

        private final String displayName;
 // 构造函数
    public ExamQuestion() {}
        QuestionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public ExamQuestion(Long paperId, QuestionType questionType, String questionContent,
                       List<String> correctAnswer, Integer score, Integer sort) {
        this.paperId = paperId;
        this.questionType = questionType;
        this.questionContent = questionContent;
        this.correctAnswer = correctAnswer;
        this.score = score;
        this.sort = sort;
    }
    public ExamQuestion(Long questionId,Long paperId, QuestionType questionType, String questionContent,
                        List<String> correctAnswer, Integer score, Integer sort) {
        this.questionId=questionId;
        this.paperId = paperId;
        this.questionType = questionType;
        this.questionContent = questionContent;
        this.correctAnswer = correctAnswer;
        this.score = score;
        this.sort = sort;
    }
    //全参构造函数
    public ExamQuestion(Long questionId,Long paperId, QuestionType questionType, String questionContent,
                        List<String> options, List<String> correctAnswer, Integer score, Integer sort) {
        this.questionId=questionId;
        this.paperId = paperId;
        this.questionType = questionType;
        this.questionContent = questionContent;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.score = score;
        this.sort = sort;
    }
    // Getters and Setters
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
    
    public QuestionType getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    
    public String getQuestionContent() {
        return questionContent;
    }
    
    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public List<String> getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(List<String> correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    
    public Long getChunkId() {
        return chunkId;
    }
    
    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public ExamPaper getExamPaper() {
        return examPaper;
    }
    
    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }
    
    public KbChunk getKbChunk() {
        return kbChunk;
    }
    
    public void setKbChunk(KbChunk kbChunk) {
        this.kbChunk = kbChunk;
    }
}