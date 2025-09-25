package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.AnalysisRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnalysisRecordDto {
    @JsonProperty("user_id")  // 匹配前端JSON的"user_id"键
    private Long userId;

    @JsonProperty("paper_id")
    private Long paperId;

    @JsonProperty("analysis_data")
    private String analysisData;

    @JsonProperty("advice_data")
    private String adviceData;
    public AnalysisRecordDto(AnalysisRecord record) {
        this.userId = record.getUserId();
        this.paperId = record.getPaperId();
        this.analysisData = record.getAnalysisData();
        this.adviceData = record.getAdviceData();
    }
}
