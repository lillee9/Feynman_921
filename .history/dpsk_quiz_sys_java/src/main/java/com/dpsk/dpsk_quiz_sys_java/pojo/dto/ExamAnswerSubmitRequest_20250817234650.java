package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.util.List;
import java.util.Map;

/**
 * 考试答案提交请求DTO
 */
public class ExamAnswerSubmitRequest {
    
    private Long paperId;
    private Long questionId;
    private String userAnswer;
    private Integer timeSpent; // 答题用时（秒）
    private java.time.LocalDateTime startTime; // 考试开始时间
    private java.time.LocalDateTime endTime; // 考试结束时间
    private Boolean override; // 是否覆盖已有记录
    
    // 批量提交答案
    private List<AnswerItem> answers;
    
    public ExamAnswerSubmitRequest() {}
    
    public ExamAnswerSubmitRequest(Long paperId, Long questionId, String userAnswer) {
        this.paperId = paperId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
    }
    
    // 内部类：答案项
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
        private Integer timeSpent;
        
        public AnswerItem() {}
        
        public AnswerItem(Long questionId, String userAnswer) {
            this.questionId = questionId;
            this.userAnswer = userAnswer;
        }
        
        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public String getUserAnswer() {
            return userAnswer;
        }
        
        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }
        
        public Integer getTimeSpent() {
            return timeSpent;
        }
        
        public void setTimeSpent(Integer timeSpent) {
            this.timeSpent = timeSpent;
        }
        
        @Override
        public String toString() {
            return "AnswerItem{" +
                    "questionId=" + questionId +
                    ", userAnswer='" + userAnswer + '\'' +
                    ", timeSpent=" + timeSpent +
                    '}';
        }
    }
    
    // Getters and Setters
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
    
    public String getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public List<AnswerItem> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public java.time.LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(java.time.LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public java.time.LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(java.time.LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Boolean getOverride() {
        return override;
    }
    
    public void setOverride(Boolean override) {
        this.override = override;
    }
    
    @Override
    public String toString() {
        return "ExamAnswerSubmitRequest{" +
                "userId=" + userId +
                ", paperId=" + paperId +
                ", questionId=" + questionId +
                ", userAnswer='" + userAnswer + '\'' +
                ", timeSpent=" + timeSpent +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", override=" + override +
                ", answers=" + answers +
                '}';
    }
}