package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

public class WrongQuestion {
    private Long questionId;  // 试题ID
    private String questionType;  // 题型（单选/多选/判断）
    private String chapter;  // 所属章节
    private String knowledgePoint;  // 关联知识点
    private String userAnswer;  // 用户答案（JSON格式）
    private String correctAnswer;  // 正确答案（JSON格式）
    private LocalDateTime answerTime;  // 答题时间

    // 全参构造函数（与analyzeUserAnswers中创建对象的参数顺序一致）
    public WrongQuestion(Long questionId, QuestionType questionType,
                         String chapter, String knowledgePoint,
                         List<String> userAnswer, List<String> correctAnswer,
                         LocalDateTime answerTime) {
        this.questionId = questionId;
        this.questionType = questionType.name();  // 枚举转字符串（假设QuestionType枚举的name()对应题型名称）
        this.chapter = chapter;  // 新增chapter参数初始化
        this.knowledgePoint = knowledgePoint;  // 新增knowledgePoint参数初始化
        this.userAnswer = String.join(",", userAnswer);  // 简单示例：将List转换为逗号分隔的字符串（实际应使用JSON序列化）
        this.correctAnswer = String.join(",", correctAnswer);  // 同上
        this.answerTime = answerTime;
    }
    public WrongQuestion(Long questionId, QuestionType questionType,
                         List<String> userAnswer, List<String> correctAnswer,
                         LocalDateTime answerTime) {
        this.questionId = questionId;
        this.questionType = questionType.name();  // 枚举转字符串（假设QuestionType枚举的name()对应题型名称）
        this.userAnswer = String.join(",", userAnswer);  // 简单示例：将List转换为逗号分隔的字符串（实际应使用JSON序列化）
        this.correctAnswer = String.join(",", correctAnswer);  // 同上
        this.answerTime = answerTime;
    }

    // 无参构造函数（用于JSON反序列化）
    public WrongQuestion() {}

    // getter 和 setter 方法
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setKnowledgePoint(String knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
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

    public LocalDateTime getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }
}