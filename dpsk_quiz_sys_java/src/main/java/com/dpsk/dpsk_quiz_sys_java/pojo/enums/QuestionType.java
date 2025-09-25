package com.dpsk.dpsk_quiz_sys_java.pojo.enums;

public enum QuestionType {
    SINGLE_CHOICE("单选题"),
    MULTI_CHOICE("多选题"),
    SHORT_ANSWER("简答题"),
    FILL_BLANK("填空题"),
    JUDGE("判断题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
