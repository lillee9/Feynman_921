package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.util.List;


//题目响应DTO

public class QuestionResponse {
    private Integer id;
    private String type;
    private String question;
    private String answer;
    private String content; // 用于流式传输的内容
    private boolean streamData; // 标识是否为流式数据

    // 选择题特有字段
    private List<String> options;
    private String explanation;

    // 简答题特有字段
    private List<String> keyPoints;

    // 填空题特有字段 (explanation字段复用)

    // 正确答案字段
    private String correctAnswer;

    public QuestionResponse() {}

    public QuestionResponse(String type, String content, boolean streamData) {
        this.type = type;
        this.content = content;
        this.streamData = streamData;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isStreamData() {
        return streamData;
    }

    public void setStreamData(boolean streamData) {
        this.streamData = streamData;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
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
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
