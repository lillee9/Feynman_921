package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试结果响应DTO
 */
public class ExamResultResponse {
    
    private Long resultId;
    private Long userId;
    private String userName; // 冗余字段，便于前端显示
    private Long paperId;
    private String paperName; // 冗余字段，便于前端显示
    private Integer totalScore;
    private Integer userScore;
    private Double scorePercentage;
    private Integer correctCount;
    private Integer totalQuestions;
    private Integer timeSpent; // 总用时（秒）
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Map<String, Object> detailAnalysis; // 详细分析数据
    private List<QuestionResult> questionResults; // 题目结果详情
    
    public ExamResultResponse() {}
    
    // 内部类：题目结果
    public static class QuestionResult {
        private Long questionId;
        private String questionContent;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private Integer score;
        private Integer timeSpent;
        
        public QuestionResult() {}
        
        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public String getQuestionContent() {
            return questionContent;
        }
        
        public void setQuestionContent(String questionContent) {
            this.questionContent = questionContent;
        }
        
        public String getUserAnswer() {
            return userAnswer;
        }
        
        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }
        
        public String getCorrectAnswer() {
            return correctAnswer;
        }
        
        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
        
        public Boolean getIsCorrect() {
            return isCorrect;
        }
        
        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }
        
        public Integer getScore() {
            return score;
        }
        
        public void setScore(Integer score) {
            this.score = score;
        }
        
        public Integer getTimeSpent() {
            return timeSpent;
        }
        
        public void setTimeSpent(Integer timeSpent) {
            this.timeSpent = timeSpent;
        }
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
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
    
    public Integer getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    
    public Integer getUserScore() {
        return userScore;
    }
    
    public void setUserScore(Integer userScore) {
        this.userScore = userScore;
    }
    
    public Double getScorePercentage() {
        return scorePercentage;
    }
    
    public void setScorePercentage(Double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }
    
    public Integer getCorrectCount() {
        return correctCount;
    }
    
    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }
    
    public Integer getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }
    
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
    
    public Map<String, Object> getDetailAnalysis() {
        return detailAnalysis;
    }
    
    public void setDetailAnalysis(Map<String, Object> detailAnalysis) {
        this.detailAnalysis = detailAnalysis;
    }
    
    public List<QuestionResult> getQuestionResults() {
        return questionResults;
    }
    
    public void setQuestionResults(List<QuestionResult> questionResults) {
        this.questionResults = questionResults;
    }
    
    @Override
    public String toString() {
        return "ExamResultResponse{" +
                "resultId=" + resultId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", paperId=" + paperId +
                ", paperName='" + paperName + '\'' +
                ", totalScore=" + totalScore +
                ", userScore=" + userScore +
                ", scorePercentage=" + scorePercentage +
                ", correctCount=" + correctCount +
                ", totalQuestions=" + totalQuestions +
                ", timeSpent=" + timeSpent +
                ", startTime=" + startTime +
                ", submitTime=" + submitTime +
                ", detailAnalysis=" + detailAnalysis +
                ", questionResults=" + questionResults +
                '}';
    }
}