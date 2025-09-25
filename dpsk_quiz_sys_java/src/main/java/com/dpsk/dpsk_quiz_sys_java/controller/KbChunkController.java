package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.KbChunkCreateRequest;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.KbChunkResponse;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IKbChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.List;
import java.util.Map;

/**
 * 知识块控制器
 */
@RestController
@RequestMapping("/kb/chunks")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class KbChunkController {
    
    @Autowired
    private IKbChunkService kbChunkService;
    
    /**
     * 获取指定文档的所有知识块列表（支持查询参数）
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<List<KbChunkResponse>>> getChunksByDocumentIdParam(
            @RequestParam Long documentId) {
        try {
            List<KbChunkResponse> chunks = kbChunkService.getChunksByDocumentId(documentId);
            return ResponseEntity.ok(ResponseMessage.success(chunks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取知识块列表失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取指定文档的所有知识块列表（路径参数）
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<ResponseMessage<List<KbChunkResponse>>> getChunksByDocumentId(
            @PathVariable Long documentId) {
        try {
            List<KbChunkResponse> chunks = kbChunkService.getChunksByDocumentId(documentId);
            return ResponseEntity.ok(ResponseMessage.success(chunks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取知识块列表失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据ID获取单个知识块详情
     */
    @GetMapping("/{chunkId}")
    public ResponseEntity<ResponseMessage<KbChunkResponse>> getChunkById(
            @PathVariable Long chunkId) {
        try {
            KbChunkResponse chunk = kbChunkService.getChunkById(chunkId);
            return ResponseEntity.ok(ResponseMessage.success(chunk));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取知识块详情失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 创建新的知识块
     */
    @PostMapping
    public ResponseEntity<ResponseMessage<KbChunkResponse>> createChunk(
            @RequestBody KbChunkCreateRequest request) {
        try {
            KbChunkResponse chunk = kbChunkService.createChunk(request);
            return ResponseEntity.ok(ResponseMessage.success(chunk));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "创建知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 更新知识块信息
     */
    @PutMapping("/{chunkId}")
    public ResponseEntity<ResponseMessage<KbChunkResponse>> updateChunk(
            @PathVariable Long chunkId,
            @RequestBody KbChunkCreateRequest request) {
        try {
            KbChunkResponse chunk = kbChunkService.updateChunk(chunkId, request);
            return ResponseEntity.ok(ResponseMessage.success(chunk));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "更新知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 删除知识块
     */
    @DeleteMapping("/{chunkId}")
    public ResponseEntity<ResponseMessage<String>> deleteChunk(
            @PathVariable Long chunkId) {
        try {
            kbChunkService.deleteChunk(chunkId);
            return ResponseEntity.ok(new ResponseMessage<>(200, "知识块删除成功", "知识块删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "删除知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 批量删除知识块
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseMessage<String>> batchDeleteChunks(
            @RequestBody Long[] chunkIds) {
        try {
            kbChunkService.batchDeleteChunks(chunkIds);
            return ResponseEntity.ok(new ResponseMessage<>(200, "批量删除知识块成功", "批量删除知识块成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "批量删除知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 搜索知识块
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseMessage<List<KbChunkResponse>>> searchChunks(
            @RequestParam String keyword,
            @RequestParam(required = false) Long documentId) {
        try {
            List<KbChunkResponse> chunks;
            if (documentId != null) {
                chunks = kbChunkService.searchChunksByDocumentId(documentId, keyword);
            } else {
                chunks = kbChunkService.searchChunks(keyword);
            }
            return ResponseEntity.ok(ResponseMessage.success(chunks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "搜索知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/stats/{documentId}")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getChunkStats(
            @PathVariable Long documentId) {
        try {
            Map<String, Object> stats = kbChunkService.getChunkStats(documentId);
            return ResponseEntity.ok(ResponseMessage.success(stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取统计信息失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据章节名称获取知识块
     */
    @GetMapping("/chapter/{chapterName}")
    public ResponseEntity<ResponseMessage<List<KbChunkResponse>>> getChunksByChapterName(
            @PathVariable String chapterName) {
        try {
            List<KbChunkResponse> chunks = kbChunkService.getChunksByChapterName(chapterName);
            return ResponseEntity.ok(ResponseMessage.success(chunks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取章节知识块失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取指定文档的知识块数量
     */
    @GetMapping("/count")
    public ResponseEntity<ResponseMessage<Long>> getChunkCountByDocumentId(
            @RequestParam Long documentId) {
        try {
            Long count = kbChunkService.getChunkCountByDocumentId(documentId);
            return ResponseEntity.ok(ResponseMessage.success(count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage<>(400, "获取知识块数量失败: " + e.getMessage(), null));
        }
    }
}