package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.enums.DifficultyLevel;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;


//题目生成请求DTO
public class QuestionGenerateRequest {
    private String ocrContent;
    private QuestionType questionType;
    private Integer questionCount;
    private DifficultyLevel difficulty;

    // 混合类型题目生成参数
    private Integer choiceCount;
    private Integer shortAnswerCount;
    private Integer fillBlankCount;

    public QuestionGenerateRequest() {}

    public QuestionGenerateRequest(String ocrContent, QuestionType questionType,
                                   Integer questionCount, DifficultyLevel difficulty) {
        this.ocrContent = ocrContent;
        this.questionType = questionType;
        this.questionCount = questionCount;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public String getOcrContent() {
        return ocrContent;
    }

    public void setOcrContent(String ocrContent) {
        this.ocrContent = ocrContent;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getChoiceCount() {
        return choiceCount;
    }

    public void setChoiceCount(Integer choiceCount) {
        this.choiceCount = choiceCount;
    }

    public Integer getShortAnswerCount() {
        return shortAnswerCount;
    }

    public void setShortAnswerCount(Integer shortAnswerCount) {
        this.shortAnswerCount = shortAnswerCount;
    }

    public Integer getFillBlankCount() {
        return fillBlankCount;
    }

    public void setFillBlankCount(Integer fillBlankCount) {
        this.fillBlankCount = fillBlankCount;
    }
}
