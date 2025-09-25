package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;

import java.util.List;
import java.util.Map;

public class ExamAnalysisResult {
    // 错题详情列表（关联WrongQuestion DTO）
    private List<WrongQuestion> wrongQuestions;
    
    // 基础统计指标
    private Integer totalQuestion;  // 总题数
    private Integer correctCount;   // 正确题数
    private Integer wrongCount;     // 错误题数
    private Integer unansweredCount; // 未答题数
    private Double correctRate;     // 正确率（0-1）
    private Integer totalCostTime;  // 总用时（分钟）
    
    // 薄弱知识点分析
    private List<String> weakKnowledgePoints;  // 按错误频率排序的知识点列表
    
    // 章节掌握情况
    private Map<String, Double> chapterCorrectRate;  // 章节名称 → 正确率（0-1）
    private Map<QuestionType, Double> questionTypeCorrectRate;  // 新增字段

    // 以下为 getter 和 setter 方法
    public void setQuestionTypeCorrectRate(Map<QuestionType, Double> questionTypeCorrectRate) {
        this.questionTypeCorrectRate = questionTypeCorrectRate;
    }
    public List<WrongQuestion> getWrongQuestions() {
        return wrongQuestions;
    }

    public void setWrongQuestions(List<WrongQuestion> wrongQuestions) {
        this.wrongQuestions = wrongQuestions;
    }

    public Integer getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(Integer totalQuestion) {
        this.totalQuestion = totalQuestion;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Integer getUnansweredCount() {
        return unansweredCount;
    }

    public void setUnansweredCount(Integer unansweredCount) {
        this.unansweredCount = unansweredCount;
    }

    public Double getCorrectRate() {
        return correctRate;
    }

    public void setCorrectRate(Double correctRate) {
        this.correctRate = correctRate;
    }

    public Integer getTotalCostTime() {
        return totalCostTime;
    }

    public void setTotalCostTime(Integer totalCostTime) {
        this.totalCostTime = totalCostTime;
    }

    public List<String> getWeakKnowledgePoints() {
        return weakKnowledgePoints;
    }

    public void setWeakKnowledgePoints(List<String> weakKnowledgePoints) {
        this.weakKnowledgePoints = weakKnowledgePoints;
    }

    public Map<String, Double> getChapterCorrectRate() {
        return chapterCorrectRate;
    }

    public void setChapterCorrectRate(Map<String, Double> chapterCorrectRate) {
        this.chapterCorrectRate = chapterCorrectRate;
    }

    public Map<QuestionType, Double> getQuestionTypeCorrectRate() {
        return questionTypeCorrectRate;
    }
}