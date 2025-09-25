package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 试卷响应DTO
 */
public class ExamPaperResponse {
    
    private Long paperId;
    private String paperName;
    private LocalDateTime generateTime;
    private Long creatorId;
    private String creatorName; // 冗余字段，便于前端显示
    private List<String> chapterScope;
    private Map<String, Integer> questionConfig;
    private ExamPaper.Difficulty difficulty;
    private Integer totalScore;
    private Integer totalQuestion;
    private ExamPaper.PaperStatus status;
    private LocalDateTime expireTime;
    private Integer questionCount; // 实际题目数量
    private Integer participantCount; // 参与人数
    
    public ExamPaperResponse() {}
    
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
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
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
    
    public ExamPaper.Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(ExamPaper.Difficulty difficulty) {
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
    
    public ExamPaper.PaperStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExamPaper.PaperStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    @Override
    public String toString() {
        return "ExamPaperResponse{" +
                "paperId=" + paperId +
                ", paperName='" + paperName + '\'' +
                ", generateTime=" + generateTime +
                ", creatorId=" + creatorId +
                ", creatorName='" + creatorName + '\'' +
                ", chapterScope=" + chapterScope +
                ", questionConfig=" + questionConfig +
                ", difficulty=" + difficulty +
                ", totalScore=" + totalScore +
                ", totalQuestion=" + totalQuestion +
                ", status=" + status +
                ", expireTime=" + expireTime +
                ", questionCount=" + questionCount +
                ", participantCount=" + participantCount +
                '}';
    }
}