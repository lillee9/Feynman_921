package com.dpsk.dpsk_quiz_sys_java.pojo.enums;

// 难度等级枚举

public enum DifficultyLevel {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String description;

    DifficultyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}