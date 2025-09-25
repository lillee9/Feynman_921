package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;
import com.dpsk.dpsk_quiz_sys_java.repository.KbChunkRepository;
import com.dpsk.dpsk_quiz_sys_java.repository.KbDocumentRepository;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IKbChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识块服务实现类
 */
@Service
public class KbChunkService implements IKbChunkService {
    
    @Autowired
    private KbChunkRepository kbChunkRepository;
    
    @Autowired
    private KbDocumentRepository kbDocumentRepository;
    
    @Override
    public List<KbChunkResponse> getChunksByDocumentId(Long documentId) {
        List<KbChunk> chunks = kbChunkRepository.findByDocumentId(documentId);
        return chunks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public KbChunkResponse getChunkById(Long chunkId) {
        Optional<KbChunk> chunkOpt = kbChunkRepository.findById(chunkId);
        if (chunkOpt.isEmpty()) {
            throw new CustomException("知识块不存在");
        }
        return convertToResponse(chunkOpt.get());
    }
    
    @Override
    public KbChunkResponse createChunk(KbChunkCreateRequest request) {
        // 验证文档是否存在
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(request.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new CustomException("文档不存在");
        }
        
        KbChunk chunk = new KbChunk();
        chunk.setDocumentId(request.getDocumentId());
        chunk.setChapterName(request.getChapterName());
        chunk.setChapter(request.getChapterName()); // 设置chapter字段
        chunk.setContent(request.getContent());
        chunk.setTokenCount(request.getContent() != null ? request.getContent().length() : 0); // 设置token_count字段
        chunk.setSortOrder(request.getSortOrder());
        chunk.setCreateTime(LocalDateTime.now());
        chunk.setUpdateTime(LocalDateTime.now());
        
        KbChunk savedChunk = kbChunkRepository.save(chunk);
        return convertToResponse(savedChunk);
    }
    
    @Override
    public KbChunkResponse updateChunk(Long chunkId, KbChunkCreateRequest request) {
        Optional<KbChunk> chunkOpt = kbChunkRepository.findById(chunkId);
        if (chunkOpt.isEmpty()) {
            throw new CustomException("知识块不存在");
        }
        
        KbChunk chunk = chunkOpt.get();
        if (request.getChapterName() != null) {
            chunk.setChapterName(request.getChapterName());
            chunk.setChapter(request.getChapterName()); // 同时更新chapter字段
        }
        if (request.getContent() != null) {
            chunk.setContent(request.getContent());
            chunk.setTokenCount(request.getContent().length()); // 同时更新token_count字段
        }
        if (request.getSortOrder() != null) {
            chunk.setSortOrder(request.getSortOrder());
        }
        chunk.setUpdateTime(LocalDateTime.now());
        
        KbChunk updatedChunk = kbChunkRepository.save(chunk);
        return convertToResponse(updatedChunk);
    }
    
    @Override
    public void deleteChunk(Long chunkId) {
        if (!kbChunkRepository.existsById(chunkId)) {
            throw new CustomException("知识块不存在");
        }
        kbChunkRepository.deleteById(chunkId);
    }
    
    @Override
    public void batchDeleteChunks(Long[] chunkIds) {
        for (Long chunkId : chunkIds) {
            if (kbChunkRepository.existsById(chunkId)) {
                kbChunkRepository.deleteById(chunkId);
            }
        }
    }
    
    @Override
    public List<KbChunkResponse> searchChunks(String keyword) {
        List<KbChunk> chunks = kbChunkRepository.findByContentContaining(keyword);
        return chunks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<KbChunkResponse> searchChunksByDocumentId(Long documentId, String keyword) {
        List<KbChunk> chunks = kbChunkRepository.findByDocumentIdAndContentContaining(documentId, keyword);
        return chunks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getChunkStats(Long documentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总知识块数量
        Long totalCount = kbChunkRepository.countByDocumentId(documentId);
        stats.put("totalCount", totalCount);
        
        // 按章节统计
        List<KbChunk> chunks = kbChunkRepository.findByDocumentId(documentId);
        Map<String, Long> chapterStats = chunks.stream()
                .collect(Collectors.groupingBy(
                    KbChunk::getChapterName,
                    Collectors.counting()
                ));
        stats.put("chapterStats", chapterStats);
        
        // 平均内容长度
        double avgContentLength = chunks.stream()
                .mapToInt(chunk -> chunk.getContent() != null ? chunk.getContent().length() : 0)
                .average()
                .orElse(0.0);
        stats.put("avgContentLength", avgContentLength);
        
        return stats;
    }
    
    @Override
    public List<KbChunkResponse> getChunksByChapterName(String chapterName) {
        List<KbChunk> chunks = kbChunkRepository.findByChapterName(chapterName);
        return chunks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long getChunkCountByDocumentId(Long documentId) {
        return kbChunkRepository.countByDocumentId(documentId);
    }
    
    /**
     * 转换为响应DTO
     */
    private KbChunkResponse convertToResponse(KbChunk chunk) {
        KbChunkResponse response = new KbChunkResponse();
        response.setChunkId(chunk.getChunkId());
        response.setDocumentId(chunk.getDocumentId());
        response.setChapterName(chunk.getChapterName());
        response.setContent(chunk.getContent());
        response.setContentLength(chunk.getContent() != null ? chunk.getContent().length() : 0);
        response.setSortOrder(chunk.getSortOrder());
        response.setCreateTime(chunk.getCreateTime());
        response.setUpdateTime(chunk.getUpdateTime());
        
        // 获取文档名称
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(chunk.getDocumentId());
        if (documentOpt.isPresent()) {
            response.setDocumentName(documentOpt.get().getFilename());
        }
        
        return response;
    }
}