package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingDto {
    private String username;  // 用户名
    private Integer score;    // 用户分数（整数）
}