package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;

import java.util.List;
import java.util.Map;

public class ChartResponse {
    private List<Double> easy;    // [优秀%, 良好%, 及格%, 不及格%]
    private List<Double> medium;
    private List<Double> hard;
    private String updateTime;    // 格式：yyyy-MM-dd HH:mm:ss
    private Integer totalUsers;   // 总参与人数
    private Integer yesterdayUsers;
    private LineChart lineChart;
    private Map<String, Map<QuestionType, Double>> barChart;

    // 新增getter/setter
    public Map<String, Map<QuestionType, Double>> getBarChart() {
        return barChart;
    }

    public void setBarChart(Map<String, Map<QuestionType, Double>> barChart) {
        this.barChart = barChart;
    }
    public static class LineChart {
        private List<String> dates;
        private List<Integer> dailyParticipants;

        // getter/setter
        public List<String> getDates() {
            return dates;
        }
        public void setDates(List<String> dates) {
            this.dates = dates;
        }
        public List<Integer> getDailyParticipants() {
            return dailyParticipants;
        }
        public void setDailyParticipants(List<Integer> dailyParticipants) {
            this.dailyParticipants = dailyParticipants;
        }
    }
    public LineChart getLineChart() {
        return lineChart;
    }

    public void setLineChart(LineChart lineChart) {
        this.lineChart = lineChart;
    }
    //getter
    public List<Double> getEasy() {
        return easy;
    }
    //setter
    public void setEasy(List<Double> easy) {
        this.easy = easy;
    }
    //getter
    public List<Double> getMedium() {
        return medium;
    }
    //setter
    public void setMedium(List<Double> medium) {
        this.medium = medium;
    }
    //getter
    public List<Double> getHard() {
        return hard;
    }
    //setter
    public void setHard(List<Double> hard) {
        this.hard = hard;
    }
    //getter
    public String getUpdateTime() {
        return updateTime;
    }
    //setter
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    //getter
    public Integer getTotalUsers() {
        return totalUsers;
    }
    //setter
    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }
    //getter
    public Integer getYesterdayUsers() {
        return yesterdayUsers;
    }
    //setter
    public void setYesterdayUsers(Integer yesterdayUsers) {
        this.yesterdayUsers = yesterdayUsers;
    }
    // 省略getter/setter（实际开发中需要补充）
}