package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import java.util.List;
import java.util.Map;

/**
 * 知识块服务接口
 */
public interface IKbChunkService {
    
    /**
     * 根据文档ID获取知识块列表
     */
    List<KbChunkResponse> getChunksByDocumentId(Long documentId);
    
    /**
     * 根据ID获取知识块详情
     */
    KbChunkResponse getChunkById(Long chunkId);
    
    /**
     * 创建知识块
     */
    KbChunkResponse createChunk(KbChunkCreateRequest request);
    
    /**
     * 更新知识块
     */
    KbChunkResponse updateChunk(Long chunkId, KbChunkCreateRequest request);
    
    /**
     * 删除知识块
     */
    void deleteChunk(Long chunkId);
    
    /**
     * 批量删除知识块
     */
    void batchDeleteChunks(Long[] chunkIds);
    
    /**
     * 搜索知识块
     */
    List<KbChunkResponse> searchChunks(String keyword);
    
    /**
     * 根据文档ID和关键词搜索知识块
     */
    List<KbChunkResponse> searchChunksByDocumentId(Long documentId, String keyword);
    
    /**
     * 获取文档的知识块统计信息
     */
    Map<String, Object> getChunkStats(Long documentId);
    
    /**
     * 根据章节名称获取知识块
     */
    List<KbChunkResponse> getChunksByChapterName(String chapterName);
    
    /**
     * 获取指定文档的知识块数量
     */
    Long getChunkCountByDocumentId(Long documentId);
}