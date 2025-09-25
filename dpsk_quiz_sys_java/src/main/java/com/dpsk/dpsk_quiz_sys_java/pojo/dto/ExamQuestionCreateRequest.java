package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import java.util.Map;

/**
 * 考试题目创建请求DTO
 */
public class ExamQuestionCreateRequest {
    
    private Long paperId;
    private QuestionType questionType;
    private String questionContent;
    private Map<String, Object> questionOptions;
    private String correctAnswer;
    private Integer score;
    private Integer sortOrder;
    private Long chunkId;
    
    public ExamQuestionCreateRequest() {}
    
    public ExamQuestionCreateRequest(Long paperId, QuestionType questionType, 
                                     String questionContent, String correctAnswer, Integer score) {
        this.paperId = paperId;
        this.questionType = questionType;
        this.questionContent = questionContent;
        this.correctAnswer = correctAnswer;
        this.score = score;
    }
    
    // Getters and Setters
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
    
    public Map<String, Object> getQuestionOptions() {
        return questionOptions;
    }
    
    public void setQuestionOptions(Map<String, Object> questionOptions) {
        this.questionOptions = questionOptions;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Long getChunkId() {
        return chunkId;
    }
    
    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }
    
    @Override
    public String toString() {
        return "ExamQuestionCreateRequest{" +
                "paperId=" + paperId +
                ", questionType=" + questionType +
                ", questionContent='" + questionContent + '\'' +
                ", questionOptions=" + questionOptions +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", score=" + score +
                ", sortOrder=" + sortOrder +
                ", chunkId=" + chunkId +
                '}';
    }
}