package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;

/**
 * 知识库文档创建请求DTO
 */
public class KbDocumentCreateRequest {
    
    private String documentName;
    private KbDocument.FileType documentType;
    private String filePath;
    private Long fileSize;
    private String description;
    private Long uploadUserId;
    
    public KbDocumentCreateRequest() {}
    
    public KbDocumentCreateRequest(String documentName, KbDocument.FileType documentType, 
                                   String filePath, Long fileSize) {
        this.documentName = documentName;
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
    
    // Getters and Setters
    public String getDocumentName() {
        return documentName;
    }
    
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
    
    public KbDocument.FileType getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(KbDocument.FileType documentType) {
        this.documentType = documentType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getUploadUserId() {
        return uploadUserId;
    }
    
    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }
    
    @Override
    public String toString() {
        return "KbDocumentCreateRequest{" +
                "documentName='" + documentName + '\'' +
                ", documentType=" + documentType +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", description='" + description + '\'' +
                ", uploadUserId=" + uploadUserId +
                '}';
    }
}