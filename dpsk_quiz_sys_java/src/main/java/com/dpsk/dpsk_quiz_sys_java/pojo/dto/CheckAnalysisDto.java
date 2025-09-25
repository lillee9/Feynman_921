package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import lombok.Data;

// 使用Lombok简化代码（需添加lombok依赖）
@Data
public class CheckAnalysisDto {
    // 字段名可根据前端需求调整（如：canAnalyze）
    private boolean canAnalyze;

    // 构造方法初始化验证结果
    public CheckAnalysisDto(boolean canAnalyze) {
        this.canAnalyze = canAnalyze;
    }
}