package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 试卷创建请求DTO
 */
public class ExamPaperCreateRequest {
    
    private String paperName;
    private List<String> chapterScope;
    private Map<String, Integer> questionConfig;
    private ExamPaper.Difficulty difficulty;
    private Integer totalScore;
    private Integer totalQuestion;
    private LocalDateTime expireTime;
    
    // 新增字段：支持三种生成方式
    private String generationType; // "file", "knowledge", "ai"
    private String topic; // AI生成主题
    private String requirements; // AI生成要求
    private List<Long> documentIds; // 知识库文档ID列表
    private List<String> chapters; // 选择的章节列表
    private String title; // 试卷标题
    private String description; // 试卷描述
    private Integer timeLimit; // 时间限制（分钟）
    private List<QuestionItem> questions; // 题目列表（用于保存生成的题目）
    
    // 内部类：题目项
    public static class QuestionItem {
        private String questionText;
        private String questionType;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
        private Integer score;
        
        // 构造函数
        public QuestionItem() {}
        
        // Getters and Setters
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        
        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
        
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
    
    public ExamPaperCreateRequest() {}
    
    public ExamPaperCreateRequest(String paperName, ExamPaper.Difficulty difficulty, 
                                  Integer totalScore, Integer totalQuestion) {
        this.paperName = paperName;
        this.difficulty = difficulty;
        this.totalScore = totalScore;
        this.totalQuestion = totalQuestion;
    }
    
    // Getters and Setters
    public String getPaperName() {
        return paperName;
    }
    
    public void setPaperName(String paperName) {
        this.paperName = paperName;
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
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    // 新增字段的 Getters and Setters
    public String getGenerationType() {
        return generationType;
    }
    
    public void setGenerationType(String generationType) {
        this.generationType = generationType;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getRequirements() {
        return requirements;
    }
    
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
    
    public List<Long> getDocumentIds() {
        return documentIds;
    }
    
    public void setDocumentIds(List<Long> documentIds) {
        this.documentIds = documentIds;
    }
    
    public List<String> getChapters() {
        return chapters;
    }
    
    public void setChapters(List<String> chapters) {
        this.chapters = chapters;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public List<QuestionItem> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
    }
    
    @Override
    public String toString() {
        return "ExamPaperCreateRequest{" +
                "paperName='" + paperName + '\'' +
                ", chapterScope=" + chapterScope +
                ", questionConfig=" + questionConfig +
                ", difficulty=" + difficulty +
                ", totalScore=" + totalScore +
                ", totalQuestion=" + totalQuestion +
                ", expireTime=" + expireTime +
                ", generationType='" + generationType + '\'' +
                ", topic='" + topic + '\'' +
                ", requirements='" + requirements + '\'' +
                ", documentIds=" + documentIds +
                ", chapters=" + chapters +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", timeLimit=" + timeLimit +
                ", questions=" + questions +
                '}';
    }
}