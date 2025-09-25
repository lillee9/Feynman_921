package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbDocument;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.repository.KbChunkRepository;
import com.dpsk.dpsk_quiz_sys_java.repository.KbDocumentRepository;
import com.dpsk.dpsk_quiz_sys_java.repository.UserRepository;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IKbDocumentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 文档解析相关导入
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.ArrayList;

@Service
public class KbDocumentService implements IKbDocumentService {
    
    @Autowired
    private KbDocumentRepository kbDocumentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String UPLOAD_DIR = "uploads/documents/";
    
    @Override
    public KbDocumentResponse uploadDocument(MultipartFile file, KbDocumentCreateRequest request, Long userId) {
        if (file.isEmpty()) {
            throw new CustomException("文件不能为空");
        }
        
        // 验证用户存在
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new CustomException("用户不存在");
        }
        
        System.out.println("开始文档上传，用户ID: " + userId);
        
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 确定文件类型
            KbDocument.FileType fileType = determineFileType(fileExtension);
            
            // 创建文档实体
            KbDocument document = new KbDocument();
            document.setFilename(request.getDocumentName() != null ? request.getDocumentName() : originalFilename);
            document.setFileType(fileType);
            document.setFileSize(file.getSize());
            document.setUploaderId(userId);
            document.setUploadTime(LocalDateTime.now());
            document.setStatus(KbDocument.ProcessStatus.uploaded);
            
            // 保存到数据库
            KbDocument savedDocument = kbDocumentRepository.save(document);
            
            // 转换为响应DTO
            return convertToResponse(savedDocument, userOpt.get().getUsername());
            
        } catch (IOException e) {
            throw new CustomException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public KbDocumentResponse getDocument(Long documentId) {
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(documentId);
        if (!documentOpt.isPresent()) {
            throw new CustomException("文档不存在");
        }
        
        KbDocument document = documentOpt.get();
        Optional<User> userOpt = userRepository.findById(document.getUploaderId());
        String uploaderName = userOpt.map(User::getUsername).orElse("未知用户");
        
        return convertToResponse(document, uploaderName);
    }
    
    @Override
    public PageResponse<KbDocumentResponse> getDocuments(com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest) {
        pageRequest.validate();
        
        Sort sort = Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().toUpperCase()), pageRequest.getSortBy());
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize(), sort);
        
        Page<KbDocument> documentPage = kbDocumentRepository.findAll(springPageRequest);
        
        List<KbDocumentResponse> responses = documentPage.getContent().stream()
                .map(doc -> {
                    Optional<User> userOpt = userRepository.findById(doc.getUploaderId());
                    String uploaderName = userOpt.map(User::getUsername).orElse("未知用户");
                    return convertToResponse(doc, uploaderName);
                })
                .collect(Collectors.toList());
        
        return PageResponse.of(responses, pageRequest.getPage(), pageRequest.getSize(), 
                documentPage.getTotalElements());
    }
    
    @Override
    public KbDocumentResponse updateDocument(Long documentId, KbDocumentCreateRequest request) {
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(documentId);
        if (!documentOpt.isPresent()) {
            throw new CustomException("文档不存在");
        }
        
        KbDocument document = documentOpt.get();
        if (request.getDocumentName() != null) {
            document.setFilename(request.getDocumentName());
        }
        if (request.getDocumentType() != null) {
            document.setFileType(request.getDocumentType());
        }
        
        KbDocument savedDocument = kbDocumentRepository.save(document);
        
        Optional<User> userOpt = userRepository.findById(document.getUploaderId());
        String uploaderName = userOpt.map(User::getUsername).orElse("未知用户");
        
        return convertToResponse(savedDocument, uploaderName);
    }
    
    @Override
    public void deleteDocument(Long documentId) {
        if (!kbDocumentRepository.existsById(documentId)) {
            throw new CustomException("文档不存在");
        }
        kbDocumentRepository.deleteById(documentId);
    }
    
    @Override
    public KbDocumentResponse processDocument(Long documentId) {
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(documentId);
        if (!documentOpt.isPresent()) {
            System.err.println("文档不存在，ID: " + documentId);
            throw new CustomException("文档不存在");
        }
        
        KbDocument document = documentOpt.get();
        document.setStatus(KbDocument.ProcessStatus.processing);
        kbDocumentRepository.save(document);
        
        // TODO: 实现文档处理逻辑（解析为知识块）
        // 这里可以调用文档解析服务，将文档内容分割为知识块
            
            // 处理完成后更新状态
            document.setStatus(KbDocument.ProcessStatus.success);
        KbDocument savedDocument = kbDocumentRepository.save(document);
        
        // 获取上传者信息并返回响应
        Optional<User> userOpt = userRepository.findById(document.getUploaderId());
        String uploaderName = userOpt.map(User::getUsername).orElse("未知用户");
        
        return convertToResponse(savedDocument, uploaderName);
    }
    
    @Override
    public PageResponse<KbDocumentResponse> getUserDocuments(Long userId, com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest) {
        pageRequest.validate();
        
        Sort sort = Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().toUpperCase()), pageRequest.getSortBy());
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize(), sort);
        
        // 由于Repository中没有分页的findByUploaderId方法，先获取所有结果然后手动分页
        List<KbDocument> allDocuments = kbDocumentRepository.findByUploaderId(userId);
        int start = springPageRequest.getPageNumber() * springPageRequest.getPageSize();
        int end = Math.min(start + springPageRequest.getPageSize(), allDocuments.size());
        List<KbDocument> pageContent = start < allDocuments.size() ? allDocuments.subList(start, end) : List.of();
        Page<KbDocument> documentPage = new PageImpl<>(pageContent, springPageRequest, allDocuments.size());
        
        Optional<User> userOpt = userRepository.findById(userId);
        String uploaderName = userOpt.map(User::getUsername).orElse("未知用户");
        
        List<KbDocumentResponse> responses = documentPage.getContent().stream()
                .map(doc -> convertToResponse(doc, uploaderName))
                .collect(Collectors.toList());
        
        return PageResponse.of(responses, pageRequest.getPage(), pageRequest.getSize(), 
                documentPage.getTotalElements());
    }
    
    private KbDocumentResponse convertToResponse(KbDocument document, String uploaderName) {
        KbDocumentResponse response = new KbDocumentResponse();
        response.setDocumentId(document.getDocumentId());
        response.setDocumentName(document.getFilename());
        response.setDocumentType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setUploadUserId(document.getUploaderId());
        response.setUploadUserName(uploaderName);
        response.setProcessStatus(document.getStatus());
        response.setUploadTime(document.getUploadTime());
        response.setChunkCount(0); // TODO: 从KbChunk表中查询实际数量
        return response;
    }
    
    private KbDocument.FileType determineFileType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case ".pdf":
                return KbDocument.FileType.PDF;
            case ".doc":
            case ".docx":
                return KbDocument.FileType.WORD;
            case ".html":
            case ".htm":
                return KbDocument.FileType.HTML;
            default:
                throw new CustomException("不支持的文件类型: " + fileExtension);
        }
    }
}