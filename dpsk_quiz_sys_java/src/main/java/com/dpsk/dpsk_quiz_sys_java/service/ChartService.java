package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ChartResponse;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import com.dpsk.dpsk_quiz_sys_java.repository.ExamResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChartService {
    // 修改为注入JPA Repository
    private final ExamResultRepository examResultRepository;

    // 构造函数同步修改
    public ChartService(ExamResultRepository examResultRepository) {
        this.examResultRepository = examResultRepository;
    }

    public ChartResponse calculateChartData() {
        ChartResponse response = new ChartResponse();

        // 调用Repository方法替代原Mapper调用
        int totalUsers = examResultRepository.countDistinctUsers();  // 原examResultMapper.countDistinctUsers()
        response.setTotalUsers(totalUsers);

        // 新增昨日人数统计
        LocalDateTime yesterdayStart = LocalDateTime.now()
                .minusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1).minusSeconds(1);
        Integer yesterdayUsers = examResultRepository.countUsersByDate(yesterdayStart, yesterdayEnd);
        response.setYesterdayUsers(yesterdayUsers);
        // 2. 获取各难度的分数段统计
        Map<String, Map<String, Integer>> statsMap = getDifficultyStats();

        // 3. 计算各难度的比例
        response.setEasy(calculateProportions(statsMap.get("easy")));
        response.setMedium(calculateProportions(statsMap.get("medium")));
        response.setHard(calculateProportions(statsMap.get("hard")));

        // 4. 设置更新时间
        response.setUpdateTime(LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

// 新增七日数据统计
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startDate = endDate.minusDays(6).withHour(0).withMinute(0).withSecond(0);

        List<Map<String, Object>> dailyData = examResultRepository.findDailyParticipants(startDate, endDate);

        // 生成日期列表并初始化0值
        List<String> dates = new ArrayList<>();
        List<Integer> participants = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        for (int i = 0; i < 7; i++) {
            dates.add(currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
            participants.add(0);
            currentDate = currentDate.plusDays(1);
        }
        // 填充实际数据
        for (Map<String, Object> row : dailyData) {
            String dateStr = (String) row.get("date");
            Integer count = ((Number) row.get("count")).intValue();
            int index = dates.indexOf(dateStr);
            if (index != -1) {
                participants.set(index, count);
            }
        }

        // 设置折线图数据
        ChartResponse.LineChart lineChart = new ChartResponse.LineChart();
        lineChart.setDates(dates);
        lineChart.setDailyParticipants(participants);
        response.setLineChart(lineChart);

        //新增柱形图数据
//        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
//        LocalDateTime startDate = endDate.minusDays(6).withHour(0).withMinute(0).withSecond(0);

        List<Map<String, Object>> accuracyData = examResultRepository.findAccuracyStats(startDate, endDate);

        Map<String, Map<QuestionType, Double>> barChart = new HashMap<>();

        // 初始化数据结构
        String[] difficulties = {"easy", "medium", "hard"};
        QuestionType[] types = QuestionType.values();
        for (String diff : difficulties) {
            Map<QuestionType, Double> typeMap = new HashMap<>();
            for (QuestionType type : types) {
                typeMap.put(type, -1.0);
            }
            barChart.put(diff, typeMap);
        }

        // 填充实际数据
        for (Map<String, Object> row : accuracyData) {
            String difficulty = ((String) row.get("difficulty")).toLowerCase();
            String typeStr = (String) row.get("question_type");
            QuestionType type = QuestionType.valueOf(typeStr);

            long correct = ((Number) row.get("correct_count")).longValue();
            long total = ((Number) row.get("total_count")).longValue();

            if (total > 0) {
                double accuracy = Math.round((correct * 100.0 / total) * 100) / 100.0;
                barChart.get(difficulty).put(type, accuracy);
            } else {
                barChart.get(difficulty).put(type, -1.0); // 保持-1表示无数据
            }
        }

        response.setBarChart(barChart);


        return response;
    }

    private Map<String, Map<String, Integer>> getDifficultyStats() {
        List<Map<String, Object>> dbResults = examResultRepository.getScoreDistribution();  // 原examResultMapper.getScoreDistribution()
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (Map<String, Object> row : dbResults) {
            String difficulty = (String) row.get("difficulty");
            Map<String, Integer> stats = new HashMap<>();
            stats.put("excellent", ((Number) row.get("excellent")).intValue());
            stats.put("good", ((Number) row.get("good")).intValue());
            stats.put("pass", ((Number) row.get("pass")).intValue());
            stats.put("fail", ((Number) row.get("fail")).intValue());
            stats.put("total", ((Number) row.get("total")).intValue());
            result.put(difficulty, stats);
        }
        return result;
    }

    private List<Double> calculateProportions(Map<String, Integer> stats) {
        List<Double> proportions = new ArrayList<>(4);
        if (stats == null) {
            return List.of(0.0, 0.0, 0.0, 0.0);
        }

        int total = stats.getOrDefault("total", 1);  // 避免除0错误
        proportions.add(round(stats.get("excellent") * 100.0 / total));
        proportions.add(round(stats.get("good") * 100.0 / total));
        proportions.add(round(stats.get("pass") * 100.0 / total));
        proportions.add(round(stats.get("fail") * 100.0 / total));
        return proportions;
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;  // 保留两位小数
    }
}