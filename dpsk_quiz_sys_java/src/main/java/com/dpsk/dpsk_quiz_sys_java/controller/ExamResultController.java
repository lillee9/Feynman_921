package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 考试结果管理控制器
 * 提供考试答案提交、成绩查询、统计分析等功能
 */
@RestController
@CrossOrigin
@RequestMapping("/exam/results")
public class ExamResultController {
    
    @Autowired
    private IExamResultService examResultService;
    
    /**
     * 提交考试答案
     */
    @PostMapping("/submit")
    public ResponseEntity<ResponseMessage<ExamResultResponse>> submitExamAnswers(
            @RequestBody ExamAnswerSubmitRequest submitRequest) {
        try {
            ExamResultResponse result = examResultService.submitExamAnswers(submitRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (ExamResultResponse)null));
        }
    }
    
    /**
     * 获取考试结果详情
     */
    @GetMapping("/{resultId}")
    public ResponseEntity<ResponseMessage<ExamResultResponse>> getExamResult(
            @PathVariable Long resultId) {
        try {
            ExamResultResponse result = examResultService.getExamResult(resultId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (ExamResultResponse)null));
        }
    }
    
    /**
     * 获取用户的考试结果列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseMessage<PageResponse<ExamResultResponse>>> getUserExamResults(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageRequest pageRequest = new PageRequest(page, size);
            PageResponse<ExamResultResponse> results = examResultService.getUserExamResults(userId, pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", results));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (PageResponse<ExamResultResponse>)null));
        }
    }
    
    /**
     * 获取试卷的所有考试结果
     */
    @GetMapping("/paper/{paperId}")
    public ResponseEntity<ResponseMessage<PageResponse<ExamResultResponse>>> getPaperExamResults(
            @PathVariable Long paperId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageRequest pageRequest = new PageRequest(page, size);
            PageResponse<ExamResultResponse> results = examResultService.getPaperExamResults(paperId, pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", results));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (PageResponse<ExamResultResponse>)null));
        }
    }
    
    /**
     * 获取用户在指定试卷的考试结果
     */
    @GetMapping("/user/{userId}/paper/{paperId}")
    public ResponseEntity<ResponseMessage<ExamResultResponse>> getUserPaperResult(
            @PathVariable Long userId,
            @PathVariable Long paperId) {
        try {
            ExamResultResponse result = examResultService.getUserPaperResult(userId, paperId);
            if (result != null) {
                return ResponseEntity.ok(new ResponseMessage<>(200, "success", result));
            } else {
                return ResponseEntity.ok(new ResponseMessage<>(404, "未找到考试结果", (ExamResultResponse)null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (ExamResultResponse)null));
        }
    }
    
    /**
     * 检查用户是否已参加考试
     */
    @GetMapping("/check/user/{userId}/paper/{paperId}")
    public ResponseEntity<ResponseMessage<Boolean>> hasUserTakenExam(
            @PathVariable Long userId,
            @PathVariable Long paperId) {
        try {
            boolean hasTaken = examResultService.hasUserTakenExam(userId, paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", hasTaken));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (Boolean)null));
        }
    }
    
    /**
     * 删除考试结果
     */
    @DeleteMapping("/{resultId}")
    public ResponseEntity<ResponseMessage<String>> deleteExamResult(
            @PathVariable Long resultId) {
        try {
            examResultService.deleteExamResult(resultId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 批量删除考试结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseMessage<String>> deleteExamResults(
            @RequestBody List<Long> resultIds) {
        try {
            examResultService.deleteExamResults(resultIds);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 获取试卷统计信息
     */
    @GetMapping("/stats/paper/{paperId}")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getPaperStatistics(
            @PathVariable Long paperId) {
        try {
            Map<String, Object> statistics = examResultService.getPaperStatistics(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", statistics));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (Map<String, Object>)null));
        }
    }
    
    /**
     * 获取用户成绩统计
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getUserStatistics(
            @PathVariable Long userId) {
        try {
            Map<String, Object> statistics = examResultService.getUserStatistics(userId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", statistics));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (Map<String, Object>)null));
        }
    }
    
    /**
     * 获取试卷排行榜
     */
    @GetMapping("/ranking/paper/{paperId}")
    public ResponseEntity<ResponseMessage<List<ExamResultResponse>>> getPaperRanking(
            @PathVariable Long paperId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<ExamResultResponse> ranking = examResultService.getPaperRanking(paperId, limit);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", ranking));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (List<ExamResultResponse>)null));
        }
    }
    
    /**
     * 计算通过率
     */
    @GetMapping("/pass-rate/paper/{paperId}")
    public ResponseEntity<ResponseMessage<Double>> calculatePassRate(
            @PathVariable Long paperId,
            @RequestParam(defaultValue = "60") BigDecimal passScore) {
        try {
            Double passRate = examResultService.calculatePassRate_ljs(paperId, passScore);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", passRate));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (Double)null));
        }
    }
    
    /**
     * 获取分数分布
     */
    @GetMapping("/score-distribution/paper/{paperId}")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getScoreDistribution(
            @PathVariable Long paperId) {
        try {
            Map<String, Object> distribution = examResultService.getScoreDistribution(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", distribution));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (Map<String, Object>)null));
        }
    }
    
    /**
     * 获取答题分析
     */
    @GetMapping("/analysis/paper/{paperId}")
    public ResponseEntity<ResponseMessage<AnalysisResponse>> getAnswerAnalysis(
            @PathVariable Long paperId) {
        try {
            AnalysisResponse analysis = examResultService.getAnswerAnalysis(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", analysis));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (AnalysisResponse)null));
        }
    }
    
    /**
     * 导出考试结果
     */
    @PostMapping("/{resultId}/recalculate")
    public ResponseEntity<ResponseMessage<ExamResultResponse>> recalculateScore(
            @PathVariable Long resultId) {
        try {
            ExamResultResponse result = examResultService.recalculateScore(resultId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (ExamResultResponse)null));
        }
    }
    
    /**
     * 获取考试结果详情
     */
    @GetMapping("/export/paper/{paperId}")
    public ResponseEntity<ResponseMessage<List<Map<String, Object>>>> getExportData(
            @PathVariable Long paperId) {
        try {
            List<Map<String, Object>> exportData = examResultService.getExportData(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", exportData));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseMessage<>(400, e.getMessage(), (List<Map<String, Object>>)null));
        }
    }
}