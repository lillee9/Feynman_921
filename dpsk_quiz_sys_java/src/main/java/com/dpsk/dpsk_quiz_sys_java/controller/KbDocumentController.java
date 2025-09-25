package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IKbDocumentService;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库文档管理控制器
 */
@RestController
@RequestMapping("/kb/documents")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class KbDocumentController {
    
    @Autowired
    private IKbDocumentService kbDocumentService;
    
    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage<KbDocumentResponse>> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam("uploadUserId") Long uploadUserId,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            KbDocumentCreateRequest request = new KbDocumentCreateRequest();
            request.setDocumentName(documentName);
            request.setDescription(description);
            request.setUploadUserId(uploadUserId);
            
            KbDocumentResponse response = kbDocumentService.uploadDocument(file, request, uploadUserId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (KbDocumentResponse)null));
        }
    }
    
    /**
     * 获取文档详情
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ResponseMessage<KbDocumentResponse>> getDocument(
            @PathVariable Long documentId) {
        
        try {
            KbDocumentResponse response = kbDocumentService.getDocument(documentId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (KbDocumentResponse)null));
        }
    }
    
    /**
     * 获取文档列表（分页）
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<KbDocumentResponse>>> getDocuments(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadTime") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {
        
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(page);
            pageRequest.setSize(size);
            pageRequest.setSortBy(sortBy);
            pageRequest.setSortDirection(sortDirection);
            
            PageResponse<KbDocumentResponse> response = kbDocumentService.getDocuments(pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (PageResponse<KbDocumentResponse>)null));
        }
    }
    
    /**
     * 获取用户上传的文档列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseMessage<PageResponse<KbDocumentResponse>>> getUserDocuments(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadTime") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {
        
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(page);
            pageRequest.setSize(size);
            pageRequest.setSortBy(sortBy);
            pageRequest.setSortDirection(sortDirection);
            
            PageResponse<KbDocumentResponse> response = kbDocumentService.getUserDocuments(userId, pageRequest);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (PageResponse<KbDocumentResponse>)null));
        }
    }
    
    /**
     * 更新文档信息
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<ResponseMessage<KbDocumentResponse>> updateDocument(
            @PathVariable Long documentId,
            @RequestBody KbDocumentCreateRequest request) {
        
        try {
            KbDocumentResponse response = kbDocumentService.updateDocument(documentId, request);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (KbDocumentResponse)null));
        }
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ResponseMessage<String>> deleteDocument(
            @PathVariable Long documentId) {
        
        try {
            kbDocumentService.deleteDocument(documentId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "文档删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 处理文档（解析文档内容）
     */
    @PostMapping("/{documentId}/process")
    public ResponseEntity<ResponseMessage<KbDocumentResponse>> processDocument(
            @PathVariable Long documentId) {
        
        try {
            KbDocumentResponse response = kbDocumentService.processDocument(documentId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (KbDocumentResponse)null));
        }
    }
    
    /**
     * 批量删除文档
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseMessage<String>> batchDeleteDocuments(
            @RequestBody Long[] documentIds) {
        
        try {
            for (Long documentId : documentIds) {
                kbDocumentService.deleteDocument(documentId);
            }
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (String)null));
        }
    }
    
    /**
     * 获取文档处理状态统计
     */
    @GetMapping("/stats/status")
    public ResponseEntity<ResponseMessage<Object>> getDocumentStatusStats() {
        try {
            // TODO: 实现文档状态统计逻辑
            // 可以返回各种处理状态的文档数量统计
            return ResponseEntity.ok(new ResponseMessage<>(200, "success", "功能开发中"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, e.getMessage(), (Object)null));
        }
    }
}