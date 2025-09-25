package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ExamQuestionCreateRequest;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ExamQuestionResponse;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/exam-question")
public class ExamQuestionController {
    
    @Autowired
    private ExamQuestionService examQuestionService;
    
    /**
     * 创建题目
     */
    @PostMapping
    public ResponseEntity<ResponseMessage<ExamQuestionResponse>> createQuestion(
            @RequestBody ExamQuestionCreateRequest request) {
        
        try {
            ExamQuestionResponse response = examQuestionService.createQuestion(request);
            return ResponseEntity.ok(ResponseMessage.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamQuestionResponse)null));
        }
    }
    
    /**
     * 基于知识块生成题目
     */
    @PostMapping("/generate")
    public ResponseEntity<ResponseMessage<ExamQuestionResponse>> createQuestionFromChunk(
            @RequestBody ExamQuestionCreateRequest request,
            @RequestParam("chunkContent") String chunkContent) {
        
        try {
            ExamQuestionResponse response = examQuestionService.createQuestionFromChunk(request, chunkContent);
            return ResponseEntity.ok(ResponseMessage.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamQuestionResponse)null));
        }
    }
    
    /**
     * 获取题目详情
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<ResponseMessage<ExamQuestionResponse>> getQuestion(
            @PathVariable Long questionId) {
        
        try {
            ExamQuestionResponse response = examQuestionService.getQuestion(questionId);
            return ResponseEntity.ok(ResponseMessage.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamQuestionResponse)null));
        }
    }
    
    /**
     * 获取试卷的所有题目
     */
    @GetMapping("/paper/{paperId}")
    public ResponseEntity<ResponseMessage<List<ExamQuestionResponse>>> getQuestionsByPaper(
            @PathVariable Long paperId,
            @RequestParam(value = "showAnswer", defaultValue = "true") boolean showAnswer) {
        
        try {
            List<ExamQuestionResponse> responses = examQuestionService.getQuestionsByPaper(paperId, showAnswer);
            return ResponseEntity.ok(ResponseMessage.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (List<ExamQuestionResponse>)null));
        }
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<ResponseMessage<ExamQuestionResponse>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody ExamQuestionCreateRequest request) {
        
        try {
            ExamQuestionResponse response = examQuestionService.updateQuestion(questionId, request);
            return ResponseEntity.ok(ResponseMessage.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamQuestionResponse)null));
        }
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ResponseMessage<String>> deleteQuestion(
            @PathVariable Long questionId) {
        
        try {
            examQuestionService.deleteQuestion(questionId);
            return ResponseEntity.ok(ResponseMessage.success("题目删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 批量删除试卷的题目
     */
    @DeleteMapping("/paper/{paperId}")
    public ResponseEntity<ResponseMessage<String>> deleteQuestionsByPaper(
            @PathVariable Long paperId) {
        
        try {
            examQuestionService.deleteQuestionsByPaper(paperId);
            return ResponseEntity.ok(ResponseMessage.success("试卷题目删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 获取题目总数
     */
    @GetMapping("/paper/{paperId}/count")
    public ResponseEntity<ResponseMessage<Long>> getQuestionCount(
            @PathVariable Long paperId) {
        
        try {
            long count = examQuestionService.getQuestionCount(paperId);
            return ResponseEntity.ok(ResponseMessage.success(count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (Long)null));
        }
    }
    
    /**
     * 调整题目顺序
     */
    @PutMapping("/{questionId}/sort")
    public ResponseEntity<ResponseMessage<String>> updateQuestionSort(
            @PathVariable Long questionId,
            @RequestParam("newSort") Integer newSort) {
        
        try {
            examQuestionService.updateQuestionSort(questionId, newSort);
            return ResponseEntity.ok(ResponseMessage.success("题目顺序调整成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 批量创建题目
     */
    @PostMapping("/batch")
    public ResponseEntity<ResponseMessage<List<ExamQuestionResponse>>> batchCreateQuestions(
            @RequestBody List<ExamQuestionCreateRequest> requests) {
        
        try {
            List<ExamQuestionResponse> responses = new java.util.ArrayList<>();
            for (ExamQuestionCreateRequest request : requests) {
                ExamQuestionResponse response = examQuestionService.createQuestion(request);
                responses.add(response);
            }
            return ResponseEntity.ok(ResponseMessage.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (List<ExamQuestionResponse>)null));
        }
    }
    
    /**
     * 批量删除题目
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseMessage<String>> batchDeleteQuestions(
            @RequestBody Long[] questionIds) {
        
        try {
            for (Long questionId : questionIds) {
                examQuestionService.deleteQuestion(questionId);
            }
            return ResponseEntity.ok(ResponseMessage.success("批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 批量更新题目顺序
     */
    @PutMapping("/batch/sort")
    public ResponseEntity<ResponseMessage<String>> batchUpdateQuestionSort(
            @RequestBody List<QuestionSortRequest> sortRequests) {
        
        try {
            for (QuestionSortRequest sortRequest : sortRequests) {
                examQuestionService.updateQuestionSort(sortRequest.getQuestionId(), sortRequest.getNewSort());
            }
            return ResponseEntity.ok(ResponseMessage.success("批量调整顺序成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 题目排序请求DTO（内部类）
     */
    public static class QuestionSortRequest {
        private Long questionId;
        private Integer newSort;
        
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public Integer getNewSort() {
            return newSort;
        }
        
        public void setNewSort(Integer newSort) {
            this.newSort = newSort;
        }
    }
}