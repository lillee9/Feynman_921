package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考试题目响应DTO
 */
public class ExamQuestionResponse {
    
    private Long questionId;
    private Long paperId;
    private String paperName; // 冗余字段，便于前端显示
    private QuestionType questionType;
    private String questionContent;
    private Map<String, Object> questionOptions;
    private String correctAnswer; // 仅管理员可见
    private String analysis; // 题目解析
    private Integer score;
    private Integer sortOrder;
    private Long chunkId;
    private String chapterName; // 冗余字段，便于前端显示
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean showAnswer; // 控制是否显示答案
    
    public ExamQuestionResponse() {}
    
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
    
    public String getPaperName() {
        return paperName;
    }
    
    public void setPaperName(String paperName) {
        this.paperName = paperName;
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
    
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
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
    
    public String getChapterName() {
        return chapterName;
    }
    
    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Boolean getShowAnswer() {
        return showAnswer;
    }
    
    public void setShowAnswer(Boolean showAnswer) {
        this.showAnswer = showAnswer;
    }
    
    @Override
    public String toString() {
        return "ExamQuestionResponse{" +
                "questionId=" + questionId +
                ", paperId=" + paperId +
                ", paperName='" + paperName + '\'' +
                ", questionType=" + questionType +
                ", questionContent='" + questionContent + '\'' +
                ", questionOptions=" + questionOptions +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", analysis='" + analysis + '\'' +
                ", score=" + score +
                ", sortOrder=" + sortOrder +
                ", chunkId=" + chunkId +
                ", chapterName='" + chapterName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", showAnswer=" + showAnswer +
                '}';
    }
}