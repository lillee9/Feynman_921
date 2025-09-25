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
@RequestMapping("/api/exam/papers")
@CrossOrigin(origins = "*")
public class ExamPaperController {
    
    @Autowired
    private IExamPaperService examPaperService;
    
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
     * 获取试卷详情
     */
    @GetMapping("/{paperId}")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> getPaper(
            @PathVariable Long paperId) {
        
        try {
            ExamPaperResponse response = examPaperService.getPaper(paperId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (ExamPaperResponse)null));
        }
    }
    
    /**
     * 获取试卷列表（分页）
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<ExamPaperResponse>>> getPapers(
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
            
            PageResponse<ExamPaperResponse> response = examPaperService.getPapers(pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (PageResponse<ExamPaperResponse>)null));
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
     * 获取可参与的试卷列表（已发布且未过期）
     */
    @GetMapping("/available")
    public ResponseEntity<ResponseMessage<PageResponse<ExamPaperResponse>>> getAvailablePapers(
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
            
            PageResponse<ExamPaperResponse> response = examPaperService.getAvailablePapers(pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (PageResponse<ExamPaperResponse>)null));
        }
    }
    
    /**
     * 更新试卷信息
     */
    @PutMapping("/{paperId}")
    public ResponseEntity<ResponseMessage<ExamPaperResponse>> updatePaper(
            @PathVariable Long paperId,
            @RequestBody ExamPaperCreateRequest request) {
        
        try {
            ExamPaperResponse response = examPaperService.updatePaper(paperId, request);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (ExamPaperResponse)null));
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
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "试卷删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (String)null));
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
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "error", (ExamPaperResponse)null));
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
}