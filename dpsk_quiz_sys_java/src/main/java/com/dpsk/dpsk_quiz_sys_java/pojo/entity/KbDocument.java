package com.dpsk.dpsk_quiz_sys_java.pojo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 知识文档表实体类
 */
@Entity
@Table(name = "kb_document")
public class KbDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;
    
    @Column(name = "filename", length = 255, nullable = false)
    private String filename;
    
    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;
    
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status = ProcessStatus.uploaded;
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    // 文档格式枚举
    public enum FileType {
        PDF, WORD, HTML, TXT
    }
    
    // 处理状态枚举
    public enum ProcessStatus {
        uploaded, processing, success, failed
    }
    
    // 构造函数
    public KbDocument() {
        this.uploadTime = LocalDateTime.now();
    }
    
    public KbDocument(String filename, String originalName, FileType fileType, Long fileSize, Long uploaderId) {
        this();
        this.filename = filename;
        this.originalName = originalName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploaderId = uploaderId;
    }
    
    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public FileType getFileType() {
        return fileType;
    }
    
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public Long getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public ProcessStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProcessStatus status) {
        this.status = status;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}