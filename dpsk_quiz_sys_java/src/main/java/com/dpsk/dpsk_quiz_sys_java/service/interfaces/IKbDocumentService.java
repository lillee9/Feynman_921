package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库文档服务接口
 */
public interface IKbDocumentService {
    
    /**
     * 上传文档
     */
    KbDocumentResponse uploadDocument(MultipartFile file, KbDocumentCreateRequest request, Long userId);
    
    /**
     * 获取文档详情
     */
    KbDocumentResponse getDocument(Long documentId);
    
    /**
     * 分页查询文档列表
     */
    PageResponse<KbDocumentResponse> getDocuments(com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest);
    
    /**
     * 更新文档信息
     */
    KbDocumentResponse updateDocument(Long documentId, KbDocumentCreateRequest request);
    
    /**
     * 删除文档
     */
    void deleteDocument(Long documentId);
    
    /**
     * 处理文档（解析为知识块）
     */
    KbDocumentResponse processDocument(Long documentId);
    
    /**
     * 获取用户的文档列表
     */
    PageResponse<KbDocumentResponse> getUserDocuments(Long userId, com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest);
}