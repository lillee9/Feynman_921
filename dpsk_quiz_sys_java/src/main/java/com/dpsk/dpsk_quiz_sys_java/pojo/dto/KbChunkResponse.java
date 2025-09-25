package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.time.LocalDateTime;

/**
 * 知识块响应DTO
 */
public class KbChunkResponse {
    
    private Long chunkId;
    private Long documentId;
    private String documentName; // 冗余字段，便于前端显示
    private String chapterName;
    private String content;
    private Integer contentLength;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public KbChunkResponse() {}
    
    // Getters and Setters
    public Long getChunkId() {
        return chunkId;
    }
    
    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }
    
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
    
    public String getChapterName() {
        return chapterName;
    }
    
    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getContentLength() {
        return contentLength;
    }
    
    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "KbChunkResponse{" +
                "chunkId=" + chunkId +
                ", documentId=" + documentId +
                ", documentName='" + documentName + '\'' +
                ", chapterName='" + chapterName + '\'' +
                ", content='" + content + '\'' +
                ", contentLength=" + contentLength +
                ", sortOrder=" + sortOrder +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}