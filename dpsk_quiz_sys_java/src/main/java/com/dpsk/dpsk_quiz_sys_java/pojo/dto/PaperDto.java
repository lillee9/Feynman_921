package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaperDto {
    private Long paperId;
    private String paperName;
    private String paperDifficulty;
    private BigDecimal totalScore;
    private LocalDateTime endTime;
    private Integer duration;
    private List<String> questionTypes;

    public PaperDto(Long paperId, String paperName, ExamPaper.Difficulty paperDifficulty, BigDecimal totalScore, LocalDateTime endTime, Integer duration, List<String> questionTypes) {
        this.paperId = paperId;
        this.paperName = paperName;
        this.paperDifficulty = String.valueOf(paperDifficulty);
        this.totalScore = totalScore;
        this.endTime = endTime;
        this.duration = duration;
        this.questionTypes = questionTypes;
    }

    //添加getter/setter
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getPaperDifficulty() {
        return paperDifficulty;
    }

    public void setPaperDifficulty(String paperDifficulty) {
        this.paperDifficulty = paperDifficulty;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<String> getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(List<String> questionTypes) {
        this.questionTypes = questionTypes;
    }
    //添加getter/setter

}