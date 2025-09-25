package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamPaperService;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;

/**
 * 考试试卷管理控制器
 */
@RestController
@RequestMapping("/exam/papers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class ExamPaperController {
    
    @Autowired
    private IExamPaperService examPaperService;
     /**
     * 创建试卷
     */
    @PostMapping("/create")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> createPaper(
            @RequestBody ExamPaperCreateRequest request) {
        
        try {
            // 预处理请求数据，确保JSON字段有合适的默认值
            preprocessRequest(request);
            
            ExamPaperResponse response = examPaperService.createPaperWithQuestions(request, 1L);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), null));
        }
    }
     /**
     * 预处理请求数据，确保JSON字段安全
     */
    private void preprocessRequest(ExamPaperCreateRequest request) {
        // 确保chapterScope不为null且长度合理
        if (request.getChapterScope() == null) {
            request.setChapterScope(new java.util.ArrayList<>());
        } else if (request.getChapterScope().size() > 50) {
            // 限制章节数量，避免JSON过长
            request.setChapterScope(request.getChapterScope().subList(0, 50));
        }
        
        // 确保questionConfig不为null且内容合理
        if (request.getQuestionConfig() == null) {
            request.setQuestionConfig(new java.util.HashMap<>());
        } else if (request.getQuestionConfig().size() > 20) {
            // 限制配置项数量，避免JSON过长
            java.util.Map<String, Integer> limitedConfig = new java.util.HashMap<>();
            int count = 0;
            for (java.util.Map.Entry<String, Integer> entry : request.getQuestionConfig().entrySet()) {
                if (count >= 20) break;
                limitedConfig.put(entry.getKey(), entry.getValue());
                count++;
            }
            request.setQuestionConfig(limitedConfig);
        }
        
        // 确保基本字段有默认值
        if (request.getTotalQuestion() == null || request.getTotalQuestion() <= 0) {
            request.setTotalQuestion(request.getQuestions() != null ? request.getQuestions().size() : 10);
        }
        
        if (request.getTotalScore() == null || request.getTotalScore() <= 0) {
            request.setTotalScore(request.getTotalQuestion() * 5); // 默认每题5分
        }
        
        if (request.getDifficulty() == null) {
            request.setDifficulty(com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper.Difficulty.medium);
        }
    }
    /**
     * 基于知识库生成试卷
     */
    @PostMapping("/create/knowledge-base")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> createPaperFromKnowledgeBase(
            @RequestBody ExamPaperCreateRequest request,
            @RequestParam("creatorId") Long creatorId) {
        
        try {
            ExamPaperResponse response = examPaperService.createPaperFromKnowledgeBase(request, creatorId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamPaperResponse)null));
        }
    }
    
    /**
     * 基于大模型生成试卷
     */
    @PostMapping("/create/ai")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> createPaperFromAI(
            @RequestBody ExamPaperCreateRequest request,
            @RequestParam("creatorId") Long creatorId,
            @RequestParam("prompt") String prompt) {
        
        try {
            ExamPaperResponse response = examPaperService.createPaperFromAI(request, creatorId, prompt);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamPaperResponse)null));
        }
    }

        /**
     * 获取试卷列表
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<ExamPaperResponse>>> getPapers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(page);
            pageRequest.setSize(size);
            PageResponse<ExamPaperResponse> pageResponse = examPaperService.getPapers(pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", pageResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", null));
        }
    }
    /**
     * 获取可用试卷列表（已发布且未过期）
     */
    @GetMapping("/available")
    public ResponseEntity<ResponseMessage<List<ExamPaperResponse>>> getAvailablePapers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(page);
            pageRequest.setSize(size);
            // 设置按生成时间降序排列，让最新的试卷排在前面
            pageRequest.setSortBy("generateTime");
            pageRequest.setSortDirection("DESC");
            PageResponse<ExamPaperResponse> pageResponse = examPaperService.getAvailablePapers(pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", pageResponse.getContent()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", null));
        }
    }
    
    /**
     * 获取用户创建的试卷列表
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ResponseMessage<PageResponse<ExamPaperResponse>>> getUserPapers(
            @PathVariable Long creatorId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "generateTime") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {
        
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(page);
            pageRequest.setSize(size);
            pageRequest.setSortBy(sortBy);
            pageRequest.setSortDirection(sortDirection);
            
            PageResponse<ExamPaperResponse> response = examPaperService.getUserPapers(creatorId, pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (PageResponse<ExamPaperResponse>)null));
        }
    }
    

     /**
     * 获取试卷详情
     */
    @GetMapping("/{paperId}")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> getPaperDetail(
            @PathVariable Long paperId) {
        
        try {
            ExamPaperResponse response = examPaperService.getPaper(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", null));
        }
    }
    
    /**
     * 更新试卷
     */
    @PutMapping("/{paperId}")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> updatePaper(
            @PathVariable Long paperId,
            @RequestBody ExamPaperCreateRequest request) {
        
        try {
            // 预处理请求数据，确保JSON字段有合适的默认值
            preprocessRequest(request);
            
            ExamPaperResponse response = examPaperService.updatePaper(paperId, request);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", null));
        }
    }
    
    /**
     * 删除试卷
     */
    @DeleteMapping("/{paperId}")
    public ResponseEntity<ResponseMessage<String>> deletePaper(
            @PathVariable Long paperId) {
        
        try {
            examPaperService.deletePaper(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "删除成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), null));
        }
    }
    
    /**
     * 发布试卷
     */
    @PostMapping("/{paperId}/publish")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> publishPaper(
            @PathVariable Long paperId) {
        
        try {
            ExamPaperResponse response = examPaperService.publishPaper(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), null));
        }
    }
    
    /**
     * 复制试卷
     */
    @PostMapping("/{paperId}/copy")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> copyPaper(
            @PathVariable Long paperId,
            @RequestParam("creatorId") Long creatorId,
            @RequestParam("newPaperName") String newPaperName) {
        
        try {
            ExamPaperResponse response = examPaperService.copyPaper(paperId, creatorId, newPaperName);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (ExamPaperResponse)null));
        }
    }
    
    /**
     * 批量删除试卷
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseMessage<String>> batchDeletePapers(
            @RequestBody Long[] paperIds) {
        
        try {
            for (Long paperId : paperIds) {
                examPaperService.deletePaper(paperId);
            }
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (String)null));
        }
    }
    
    /**
     * 获取试卷统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ResponseMessage<Object>> getPaperStats(
            @RequestParam(value = "creatorId", required = false) Long creatorId) {
        
        try {
            // TODO: 实现试卷统计逻辑
            // 可以返回试卷总数、已发布数量、草稿数量等统计信息
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "功能开发中"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (Object)null));
        }
    }
    /**
     * 取消发布试卷
     */
    @PostMapping("/{paperId}/unpublish")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> unpublishPaper(
            @PathVariable Long paperId) {
        
        try {
            ExamPaperResponse response = examPaperService.unpublishPaper(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), null));
        }
    }
}