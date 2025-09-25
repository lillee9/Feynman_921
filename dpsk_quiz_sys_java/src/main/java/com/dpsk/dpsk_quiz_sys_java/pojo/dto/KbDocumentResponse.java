package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;
import java.time.LocalDateTime;

/**
 * 知识库文档响应DTO
 */
public class KbDocumentResponse {
    
    private Long documentId;
    private String documentName;
    private KbDocument.FileType documentType;
    private String filePath;
    private Long fileSize;
    private String description;
    private Long uploadUserId;
    private String uploadUserName; // 冗余字段，便于前端显示
    private KbDocument.ProcessStatus processStatus;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;
    private Integer chunkCount; // 关联的知识块数量
    
    public KbDocumentResponse() {}
    
    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
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
    
    public String getUploadUserName() {
        return uploadUserName;
    }
    
    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }
    
    public KbDocument.ProcessStatus getProcessStatus() {
        return processStatus;
    }
    
    public void setProcessStatus(KbDocument.ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getChunkCount() {
        return chunkCount;
    }
    
    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }
    
    @Override
    public String toString() {
        return "KbDocumentResponse{" +
                "documentId=" + documentId +
                ", documentName='" + documentName + '\'' +
                ", documentType=" + documentType +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", description='" + description + '\'' +
                ", uploadUserId=" + uploadUserId +
                ", uploadUserName='" + uploadUserName + '\'' +
                ", processStatus=" + processStatus +
                ", uploadTime=" + uploadTime +
                ", updateTime=" + updateTime +
                ", chunkCount=" + chunkCount +
                '}';
    }
}