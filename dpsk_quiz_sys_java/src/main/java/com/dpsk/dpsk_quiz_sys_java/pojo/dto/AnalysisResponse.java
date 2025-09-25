package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分析数据响应DTO
 */
public class AnalysisResponse {
    
    // 题目分析数据
    private List<QuestionAnalysis> questionAnalysisList;
    
    // 知识点分析数据
    private List<KnowledgeAnalysis> knowledgeAnalysisList;
    
    // 整体统计数据
    private OverallStatistics overallStatistics;
    
    public AnalysisResponse() {}
    
    // 内部类：题目分析
    public static class QuestionAnalysis {
        private Long questionId;
        private String questionContent;
        private String chapterName;
        private Integer totalAnswers;
        private Integer correctAnswers;
        private Double correctRate;
        private Double averageScore;
        private LocalDateTime lastUpdated;
        
        public QuestionAnalysis() {}
        
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
        
        public String getChapterName() {
            return chapterName;
        }
        
        public void setChapterName(String chapterName) {
            this.chapterName = chapterName;
        }
        
        public Integer getTotalAnswers() {
            return totalAnswers;
        }
        
        public void setTotalAnswers(Integer totalAnswers) {
            this.totalAnswers = totalAnswers;
        }
        
        public Integer getCorrectAnswers() {
            return correctAnswers;
        }
        
        public void setCorrectAnswers(Integer correctAnswers) {
            this.correctAnswers = correctAnswers;
        }
        
        public Double getCorrectRate() {
            return correctRate;
        }
        
        public void setCorrectRate(Double correctRate) {
            this.correctRate = correctRate;
        }
        
        public Double getAverageScore() {
            return averageScore;
        }
        
        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }
        
        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }
        
        public void setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
    
    // 内部类：知识点分析
    public static class KnowledgeAnalysis {
        private Long chunkId;
        private String chapterName;
        private String content;
        private Integer questionCount;
        private Double averageCorrectRate;
        private String masteryLevel; // 掌握程度：excellent, good, fair, poor
        private LocalDateTime lastUpdated;
        
        public KnowledgeAnalysis() {}
        
        // Getters and Setters
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
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Integer getQuestionCount() {
            return questionCount;
        }
        
        public void setQuestionCount(Integer questionCount) {
            this.questionCount = questionCount;
        }
        
        public Double getAverageCorrectRate() {
            return averageCorrectRate;
        }
        
        public void setAverageCorrectRate(Double averageCorrectRate) {
            this.averageCorrectRate = averageCorrectRate;
        }
        
        public String getMasteryLevel() {
            return masteryLevel;
        }
        
        public void setMasteryLevel(String masteryLevel) {
            this.masteryLevel = masteryLevel;
        }
        
        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }
        
        public void setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
    
    // 内部类：整体统计
    public static class OverallStatistics {
        private Integer totalQuestions;
        private Integer totalAnswers;
        private Double overallCorrectRate;
        private Double averageScore;
        private Map<String, Integer> chapterDistribution;
        private Map<String, Double> difficultyAnalysis;
        private List<String> weakKnowledgePoints;
        private List<String> strongKnowledgePoints;
        
        public OverallStatistics() {}
        
        // Getters and Setters
        public Integer getTotalQuestions() {
            return totalQuestions;
        }
        
        public void setTotalQuestions(Integer totalQuestions) {
            this.totalQuestions = totalQuestions;
        }
        
        public Integer getTotalAnswers() {
            return totalAnswers;
        }
        
        public void setTotalAnswers(Integer totalAnswers) {
            this.totalAnswers = totalAnswers;
        }
        
        public Double getOverallCorrectRate() {
            return overallCorrectRate;
        }
        
        public void setOverallCorrectRate(Double overallCorrectRate) {
            this.overallCorrectRate = overallCorrectRate;
        }
        
        public Double getAverageScore() {
            return averageScore;
        }
        
        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }
        
        public Map<String, Integer> getChapterDistribution() {
            return chapterDistribution;
        }
        
        public void setChapterDistribution(Map<String, Integer> chapterDistribution) {
            this.chapterDistribution = chapterDistribution;
        }
        
        public Map<String, Double> getDifficultyAnalysis() {
            return difficultyAnalysis;
        }
        
        public void setDifficultyAnalysis(Map<String, Double> difficultyAnalysis) {
            this.difficultyAnalysis = difficultyAnalysis;
        }
        
        public List<String> getWeakKnowledgePoints() {
            return weakKnowledgePoints;
        }
        
        public void setWeakKnowledgePoints(List<String> weakKnowledgePoints) {
            this.weakKnowledgePoints = weakKnowledgePoints;
        }
        
        public List<String> getStrongKnowledgePoints() {
            return strongKnowledgePoints;
        }
        
        public void setStrongKnowledgePoints(List<String> strongKnowledgePoints) {
            this.strongKnowledgePoints = strongKnowledgePoints;
        }
    }
    
    // Getters and Setters
    public List<QuestionAnalysis> getQuestionAnalysisList() {
        return questionAnalysisList;
    }
    
    public void setQuestionAnalysisList(List<QuestionAnalysis> questionAnalysisList) {
        this.questionAnalysisList = questionAnalysisList;
    }
    
    public List<KnowledgeAnalysis> getKnowledgeAnalysisList() {
        return knowledgeAnalysisList;
    }
    
    public void setKnowledgeAnalysisList(List<KnowledgeAnalysis> knowledgeAnalysisList) {
        this.knowledgeAnalysisList = knowledgeAnalysisList;
    }
    
    public OverallStatistics getOverallStatistics() {
        return overallStatistics;
    }
    
    public void setOverallStatistics(OverallStatistics overallStatistics) {
        this.overallStatistics = overallStatistics;
    }
    
    @Override
    public String toString() {
        return "AnalysisResponse{" +
                "questionAnalysisList=" + questionAnalysisList +
                ", knowledgeAnalysisList=" + knowledgeAnalysisList +
                ", overallStatistics=" + overallStatistics +
                '}';
    }
}