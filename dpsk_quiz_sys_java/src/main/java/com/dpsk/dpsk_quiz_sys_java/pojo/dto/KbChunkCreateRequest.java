package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

/**
 * 知识块创建请求DTO
 */
public class KbChunkCreateRequest {
    
    private Long documentId;
    private String chapterName;
    private String content;
    private Integer sortOrder;
    
    public KbChunkCreateRequest() {}
    
    public KbChunkCreateRequest(Long documentId, String chapterName, String content) {
        this.documentId = documentId;
        this.chapterName = chapterName;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
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
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    @Override
    public String toString() {
        return "KbChunkCreateRequest{" +
                "documentId=" + documentId +
                ", chapterName='" + chapterName + '\'' +
                ", content='" + content + '\'' +
                ", sortOrder=" + sortOrder +
                '}';
    }
}