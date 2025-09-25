package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {
    
    // 根据文档名称查找
    Optional<KbDocument> findByFilename(String filename);
    
    // 根据文档类型查找
    List<KbDocument> findByFileType(KbDocument.FileType fileType);
    
    // 根据上传用户ID查找文档
    List<KbDocument> findByUploaderId(Long uploaderId);
    
    // 根据状态查找
    List<KbDocument> findByStatus(String status);
    
    // 根据上传时间范围查找
    List<KbDocument> findByUploadTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据文档大小范围查找
    List<KbDocument> findByFileSizeBetween(Long minSize, Long maxSize);
    
    // 根据上传用户ID和状态查找文档
    List<KbDocument> findByUploaderIdAndStatus(Long uploaderId, KbDocument.ProcessStatus status);
    
    // 根据文档名称模糊查询
    @Query("SELECT d FROM KbDocument d WHERE d.filename LIKE :keyword")
    List<KbDocument> findByFilenameContaining(@Param("keyword") String keyword);
    
    // 统计指定用户上传的文档数量
    @Query("SELECT COUNT(d) FROM KbDocument d WHERE d.uploaderId = :userId")
    Long countByUploaderId(@Param("userId") Long userId);
    
    // 统计指定状态的文档数量
    Long countByStatus(String status);
    
    // 查找最近上传的文档
    @Query("SELECT d FROM KbDocument d ORDER BY d.uploadTime DESC")
    List<KbDocument> findRecentDocuments();
}