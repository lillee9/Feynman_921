package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

public class AnalysisContentDto {
    private String content;  // AI生成的分析内容片段

    // 无参构造函数（用于JSON反序列化）
    public AnalysisContentDto() {}

    // 带参构造函数（用于快速初始化）
    public AnalysisContentDto(String content) {
        this.content = content;
    }

    // Getter 和 Setter
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}