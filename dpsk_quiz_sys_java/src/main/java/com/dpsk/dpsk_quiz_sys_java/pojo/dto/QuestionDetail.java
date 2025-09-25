package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.util.List;


//题目详情DTO (用于结构化返回)
public class QuestionDetail {
    private Integer id;
    private String type;
    private String question;
    private String answer;
    private String difficulty;
    private Long createdTime;

    // 选择题字段
    private List<String> options;
    private String correctOption;
    private String explanation;

    // 简答题字段
    private List<String> keyPoints;
    private Integer minWords; // 最少字数要求

    // 填空题字段
    private List<String> blanks; // 多个空格的答案
    private List<Integer> blankPositions; // 空格位置

    public QuestionDetail() {
        this.createdTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public Integer getMinWords() {
        return minWords;
    }

    public void setMinWords(Integer minWords) {
        this.minWords = minWords;
    }

    public List<String> getBlanks() {
        return blanks;
    }

    public void setBlanks(List<String> blanks) {
        this.blanks = blanks;
    }

    public List<Integer> getBlankPositions() {
        return blankPositions;
    }

    public void setBlankPositions(List<Integer> blankPositions) {
        this.blankPositions = blankPositions;
    }
}
