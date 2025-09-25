package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import com.dpsk.dpsk_quiz_sys_java.service.VectorStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语义搜索控制器
 */
@RestController
@RequestMapping("/semantic")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class SemanticSearchController {
    
    @Autowired
    private VectorStorageService vectorStorageService;
    
    /**
     * 语义搜索接口
     * @param queryText 查询文本
     * @param topK 返回结果数量，默认为5
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> semanticSearch(
            @RequestParam("query") String queryText,
            @RequestParam(value = "topK", defaultValue = "5") int topK) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (queryText == null || queryText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "查询文本不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 执行语义搜索
            List<KbChunk> results = vectorStorageService.semanticSearch(queryText.trim(), topK);
            
            response.put("success", true);
            response.put("message", "搜索完成");
            response.put("data", results);
            response.put("total", results.size());
            response.put("query", queryText.trim());
            
            System.out.println("语义搜索完成，查询: \"" + queryText + "\", 返回结果: " + results.size() + " 个");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("语义搜索失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取向量存储统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getVectorStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int vectorCount = vectorStorageService.getVectorCount();
            
            response.put("success", true);
            response.put("message", "获取统计信息成功");
            response.put("vectorCount", vectorCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("获取向量统计失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}