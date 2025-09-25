package com.dpsk.dpsk_quiz_sys_java.repository;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {
    
    // 根据文档ID查找所有知识块
    List<KbChunk> findByDocumentId(Long documentId);
    
    // 根据章节查找知识块
    List<KbChunk> findByChapter(String chapter);
       // 根据章节查找知识块
    List<KbChunk> findByChapterName(String chapterName);
    
    // 根据章节名称列表查找知识块
    List<KbChunk> findByChapterIn(List<String> chapterNames);
      // 根据章节名称列表查找知识块
    List<KbChunk> findByChapterNameIn(List<String> chapterNames);
    
    // 根据文档ID和章节查找知识块
    List<KbChunk> findByDocumentIdAndChapterName(Long documentId, String chapterName);
    
    // 根据文档ID和章节查找知识块
    List<KbChunk> findByDocumentIdAndChapter(Long documentId, String chapter);
    
    // 根据知识块ID排序查找
    List<KbChunk> findByDocumentIdOrderByChunkId(Long documentId);
    
    // 根据内容长度范围查找（使用自定义查询）
    @Query("SELECT c FROM KbChunk c WHERE LENGTH(c.content) BETWEEN :minLength AND :maxLength")
    List<KbChunk> findByContentLengthBetween(@Param("minLength") Integer minLength, @Param("maxLength") Integer maxLength);
    
    // 根据文档ID和内容长度范围查找（使用自定义查询）
    @Query("SELECT c FROM KbChunk c WHERE c.documentId = :documentId AND LENGTH(c.content) BETWEEN :minLength AND :maxLength")
    List<KbChunk> findByDocumentIdAndContentLengthBetween(@Param("documentId") Long documentId, @Param("minLength") Integer minLength, @Param("maxLength") Integer maxLength);
    
    // 根据内容关键词模糊查询
    @Query("SELECT c FROM KbChunk c WHERE c.content LIKE %:keyword%")
    List<KbChunk> findByContentContaining(@Param("keyword") String keyword);
    
    // 根据文档ID和内容关键词查找
    @Query("SELECT c FROM KbChunk c WHERE c.documentId = :documentId AND c.content LIKE %:keyword%")
    List<KbChunk> findByDocumentIdAndContentContaining(@Param("documentId") Long documentId, @Param("keyword") String keyword);
    
    // 统计指定文档的知识块数量
    Long countByDocumentId(Long documentId);
    
    // 统计指定章节的知识块数量
    Long countByChapter(String chapter);
        // 统计指定章节的知识块数量
    Long countByChapterName(String chapterName);
    // 查找指定文档的最大知识块ID
    @Query("SELECT MAX(c.chunkId) FROM KbChunk c WHERE c.documentId = :documentId")
    Long findMaxChunkIdByDocumentId(@Param("documentId") Long documentId);
    
    // 根据文档ID删除所有知识块
    @Modifying
    @Transactional
    @Query("DELETE FROM KbChunk c WHERE c.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);
    
    // 查找指定文档的所有章节
    @Query("SELECT DISTINCT c.chapterName FROM KbChunk c WHERE c.documentId = :documentId ORDER BY c.chapterName")
    List<String> findDistinctChaptersByDocumentId(@Param("documentId") Long documentId);
    
    // 根据章节和内容长度查找
    @Query("SELECT c FROM KbChunk c WHERE c.chapterName = :chapterName AND LENGTH(c.content) >= :minLength ORDER BY c.chunkId")
    List<KbChunk> findByChapterNameAndMinContentLength(@Param("chapterName") String chapterName, @Param("minLength") Integer minLength);
}