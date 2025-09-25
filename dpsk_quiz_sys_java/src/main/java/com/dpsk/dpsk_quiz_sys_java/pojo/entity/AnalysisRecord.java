package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "analysis_records")  // 映射数据库表名
public class AnalysisRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自增主键
    private Integer id;

    @Column(name = "user_id")  // 映射数据库列名
    private Long userId;

    @Column(name = "paper_id")
    private Long paperId;

    @Column(name = "analysis_data", columnDefinition = "TEXT")  // 指定TEXT类型
    private String analysisData;

    @Column(name = "advice_data", columnDefinition = "TEXT")
    private String adviceData;

    // update_time由数据库自动维护，无需Java字段
}