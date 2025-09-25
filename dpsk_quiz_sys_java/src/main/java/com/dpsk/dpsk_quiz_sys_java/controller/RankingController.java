package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.RankingDto;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamResult;
import com.dpsk.dpsk_quiz_sys_java.service.ExamResultService;
import com.dpsk.dpsk_quiz_sys_java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RankingController {

    @Autowired
    private ExamResultService examResultService;  // 考试结果表Service
    @Autowired
    private UserService userService;  // 用户表Service

    /**
     * 获取不同难度的考试排行榜（前5名）
     * @param difficulty 难度参数（easy/medium/hard）
     * @return 包含用户名和分数的排行榜列表
     */
    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings(@RequestParam String difficulty) {
        // 1. 参数校验并转换为枚举类型
        String lowerDifficulty = difficulty.toLowerCase();
        List<String> validDifficulties = Arrays.stream(ExamPaper.Difficulty.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        if (!validDifficulties.contains(lowerDifficulty)) {
            return ResponseEntity.badRequest().body("无效的难度参数，可选值：" + validDifficulties);
        }

        // 2. 字符串转换为枚举实例
        ExamPaper.Difficulty difficultyEnum = ExamPaper.Difficulty.valueOf(lowerDifficulty);

        // 3. 查询该难度下分数前5的考试结果（按总分降序）
        List<ExamResult> topResults = examResultService.getTopResultsByDifficulty(difficultyEnum, 5);

        // 4. 转换为包含用户名的排行榜DTO（原有逻辑不变）
        List<RankingDto> rankings = topResults.stream()
                .map(result -> {
                    String username = userService.getUsernameById(result.getUserId());
                    int score = result.getTotalScore().intValue();
                    return new RankingDto(username, score);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(rankings);
    }
}