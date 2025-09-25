package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.math.BigDecimal;

public class ScoreDto {
    private BigDecimal score;    // 答题分数
    private Long duration;    // 答题时间（单位：秒）

    public ScoreDto(BigDecimal score, Integer duration) {
        this.score = score;
        this.duration = Long.valueOf(duration);
    }

    // Getters
    public BigDecimal getScore() { return score; }
    public Long getDuration() { return duration; }
}
