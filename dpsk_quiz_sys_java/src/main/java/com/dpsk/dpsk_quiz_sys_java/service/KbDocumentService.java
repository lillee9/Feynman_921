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
// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;

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
    private VectorStorageService vectorStorageService;
    
    @Autowired
    private KbDocumentRepository kbDocumentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KbChunkRepository kbChunkRepository;
    
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
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new CustomException("文件名不能为空");
            }
            
            System.out.println("原始文件名: " + originalFilename);
            
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            System.out.println("生成的唯一文件名: " + uniqueFilename);
            System.out.println("UPLOAD_DIR: " + UPLOAD_DIR);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 确定文件类型
            KbDocument.FileType fileType = determineFileType(fileExtension);
            
            // 创建文档实体
            KbDocument document = new KbDocument();
            // 保存原始文件名到originalName字段
            String originalName = request.getDocumentName() != null ? request.getDocumentName() : originalFilename;
            document.setOriginalName(originalName);
            // filename字段用于存储唯一的文件名（稍后会更新为包含ID的格式）
            document.setFilename(originalName);
            document.setFileType(fileType);
            document.setFileSize(file.getSize());
            document.setUploaderId(userId);
            document.setUploadTime(LocalDateTime.now());
            document.setStatus(KbDocument.ProcessStatus.uploaded);
            
            // 设置临时文件路径，确保不为null
            String tempFilePath = UPLOAD_DIR + uniqueFilename;
            if (tempFilePath == null || tempFilePath.trim().isEmpty()) {
                throw new CustomException("文件路径生成失败");
            }
            
            System.out.println("设置临时文件路径: " + tempFilePath);
            document.setFilePath(tempFilePath);
            
            // 保存文件路径到数据库（需要先保存实体以获取ID）
            System.out.println("保存文档实体到数据库，filePath: " + document.getFilePath());
            KbDocument savedDocument = kbDocumentRepository.save(document);
            System.out.println("文档保存成功，获得ID: " + savedDocument.getDocumentId());
            
            // 重新设置文件名包含ID，确保唯一性
            String finalFilename = savedDocument.getDocumentId() + "_" + uniqueFilename;
            Path finalFilePath = uploadPath.resolve(finalFilename);
            
            System.out.println("最终文件名: " + finalFilename);
            
            // 如果临时文件存在，重命名为最终文件名
            if (Files.exists(filePath)) {
                Files.move(filePath, finalFilePath);
                System.out.println("文件重命名成功: " + finalFilePath.toString());
            }
            
            // 更新文档实体的文件路径
            String finalFilePathStr = UPLOAD_DIR + finalFilename;
            if (finalFilePathStr == null || finalFilePathStr.trim().isEmpty()) {
                throw new CustomException("最终文件路径生成失败");
            }
            
            System.out.println("设置最终文件路径: " + finalFilePathStr);
            savedDocument.setFilename(finalFilename);
            savedDocument.setFilePath(finalFilePathStr);
            savedDocument = kbDocumentRepository.save(savedDocument);
            System.out.println("文档更新成功");
            
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
            // 更新原始文件名而非filename
            document.setOriginalName(request.getDocumentName());
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
        
        // 先删除该文档关联的所有知识块，避免外键约束错误
        kbChunkRepository.deleteByDocumentId(documentId);
        
        // 再删除文档本身
        kbDocumentRepository.deleteById(documentId);
    }
    
    @Override
    public KbDocumentResponse processDocument(Long documentId) {
        System.out.println("开始处理文档，ID: " + documentId);
        Optional<KbDocument> documentOpt = kbDocumentRepository.findById(documentId);
        if (!documentOpt.isPresent()) {
            System.err.println("文档不存在，ID: " + documentId);
            throw new CustomException("文档不存在");
        }
        
        KbDocument document = documentOpt.get();
        System.out.println("文档信息 - ID: " + document.getDocumentId() + ", 文件名: " + document.getFilename() + ", 原始名: " + document.getOriginalName());
        
        document.setStatus(KbDocument.ProcessStatus.processing);
        kbDocumentRepository.save(document);
        
        try {
            // 实现文档处理逻辑（解析为知识块）
            String filePath = UPLOAD_DIR + document.getFilename();
            System.out.println("构建的文件路径: " + filePath);
            
            File file = new File(filePath);
            System.out.println("文件是否存在: " + file.exists());
            System.out.println("文件绝对路径: " + file.getAbsolutePath());
            
            if (!file.exists()) {
                System.err.println("文件不存在: " + filePath);
                System.err.println("尝试查找文件目录内容:");
                File uploadDir = new File(UPLOAD_DIR);
                if (uploadDir.exists() && uploadDir.isDirectory()) {
                    File[] files = uploadDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            System.err.println("  - " + f.getName());
                        }
                    }
                } else {
                    System.err.println("上传目录不存在: " + UPLOAD_DIR);
                }
                throw new CustomException("文件不存在: " + filePath);
            }
            
            // 根据文件类型选择不同的解析策略
            String content = "";
            try {
                switch (document.getFileType()) {
                    case TXT:
                        content = parseTxtDocument(file);
                        break;
                    case PDF:
                        content = parsePdfDocument(file);
                        break;
                    case WORD:
                        content = parseWordDocument(file);
                        break;
                    case HTML:
                        content = parseHtmlDocument(file);
                        break;
                    default:
                        throw new CustomException("不支持的文件类型: " + document.getFileType());
                }
            } catch (Exception e) {
                System.err.println("文档解析失败: " + e.getMessage());
                e.printStackTrace();
                throw new CustomException("文档解析失败: " + e.getMessage());
            }
            
            // 使用语义分块功能将内容分割为知识块
            List<KbChunk> chunks = performSemanticChunking(content, documentId);
            System.out.println("生成知识块数量: " + chunks.size());
            
            // 删除旧的知识块
            kbChunkRepository.deleteByDocumentId(documentId);
            
            // 保存知识块到数据库
            List<KbChunk> savedChunks = kbChunkRepository.saveAll(chunks);
            System.out.println("成功保存 " + savedChunks.size() + " 个知识块到数据库");
            
            // 生成并存储向量
            try {
                vectorStorageService.storeChunkVectors(savedChunks);
                System.out.println("成功为 " + savedChunks.size() + " 个知识块生成向量并存储");
            } catch (Exception e) {
                System.err.println("向量存储失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 处理完成后更新状态
            document.setStatus(KbDocument.ProcessStatus.success);
            
        } catch (Exception e) {
            // 处理失败时设置失败状态
            document.setStatus(KbDocument.ProcessStatus.failed);
            // 先保存失败状态到数据库
            kbDocumentRepository.save(document);
            // 然后抛出异常
            throw new CustomException("文档处理失败: " + e.getMessage());
        }
        
        // 保存成功状态到数据库
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
        // 使用originalName而非filename作为documentName，解决文档名称显示问题
        response.setDocumentName(document.getOriginalName());
        response.setDocumentType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setUploadUserId(document.getUploaderId());
        response.setUploadUserName(uploaderName);
        response.setProcessStatus(document.getStatus());
        response.setUploadTime(document.getUploadTime());
        
        // 从KbChunk表中查询实际数量
        Long chunkCount = kbChunkRepository.countByDocumentId(document.getDocumentId());
        response.setChunkCount(chunkCount != null ? chunkCount.intValue() : 0);
        
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
            case ".txt":
                return KbDocument.FileType.TXT;
            default:
                throw new CustomException("不支持的文件类型: " + fileExtension);
        }
    }
    
    private String parsePdfDocument(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private String parseWordDocument(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".docx")) {
            return parseDocxDocument(file);
        } else if (fileName.endsWith(".doc")) {
            return parseDocDocument(file);
        } else {
            throw new IOException("不支持的Word文档格式");
        }
    }
    
    private String parseDocxDocument(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }
    
    private String parseDocDocument(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }
    
    private String parseHtmlDocument(File file) throws IOException {
        // Document doc = Jsoup.parse(file, "UTF-8");
        // return doc.text();
        // 临时注释jsoup相关代码
        return "HTML文档解析暂时不可用";
    }
    
    /**
     * 解析TXT文档
     * @param file 文件对象
     * @return 文档内容
     * @throws IOException IO异常
     */
    private String parseTxtDocument(File file) throws IOException {
        // 尝试多种编码格式读取TXT文件
        String[] encodings = {"UTF-8", "GBK", "GB2312", "ISO-8859-1"};
        
        for (String encoding : encodings) {
            try {
                String content = Files.readString(file.toPath(), Charset.forName(encoding));
                // 检查是否包含乱码字符（简单检测）
                if (!content.contains("?") || encoding.equals("ISO-8859-1")) {
                    System.out.println("成功使用编码 " + encoding + " 读取TXT文件: " + file.getName());
                    return content;
                }
            } catch (Exception e) {
                System.out.println("使用编码 " + encoding + " 读取文件失败: " + e.getMessage());
                continue;
            }
        }
        
        // 如果所有编码都失败，使用默认UTF-8
        System.out.println("所有编码尝试失败，使用UTF-8作为默认编码");
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
    
    /**
     * 语义分块功能：将文档内容按照512-1024 tokens进行智能切割
     * @param content 文档内容
     * @param documentId 文档ID
     * @return 知识块列表
     */
    private List<KbChunk> performSemanticChunking(String content, Long documentId) {
        List<KbChunk> chunks = new ArrayList<>();
        
        // 预处理：清理文本
        String cleanedContent = cleanText(content);
        
        // 按句子分割
        String[] sentences = splitIntoSentences(cleanedContent);
        
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 1;
        int currentTokenCount = 0;
        final int MIN_CHUNK_SIZE = 512;
        final int MAX_CHUNK_SIZE = 1024;
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            int sentenceTokens = estimateTokenCount(sentence);
            
            // 如果当前块加上新句子会超过最大限制，且当前块已达到最小要求，则创建新块
            if (currentTokenCount + sentenceTokens > MAX_CHUNK_SIZE && currentTokenCount >= MIN_CHUNK_SIZE) {
                if (currentChunk.length() > 0) {
                    createChunk(chunks, currentChunk.toString(), documentId, chunkIndex++, currentTokenCount);
                    currentChunk = new StringBuilder();
                    currentTokenCount = 0;
                }
            }
            
            // 添加句子到当前块
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
            currentTokenCount += sentenceTokens;
            
            // 如果单个句子就超过最大限制，强制创建块
            if (currentTokenCount >= MAX_CHUNK_SIZE) {
                createChunk(chunks, currentChunk.toString(), documentId, chunkIndex++, currentTokenCount);
                currentChunk = new StringBuilder();
                currentTokenCount = 0;
            }
        }
        
        // 处理最后一个块
        if (currentChunk.length() > 0) {
            createChunk(chunks, currentChunk.toString(), documentId, chunkIndex, currentTokenCount);
        }
        
        return chunks;
    }
    
    /**
     * 清理文本：去除多余空白、特殊字符等
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // 去除多余的空白字符
        text = text.replaceAll("\\s+", " ");
        // 去除特殊控制字符
        text = text.replaceAll("[\\x00-\\x1F\\x7F]", "");
        // 标准化换行符
        text = text.replaceAll("\\r\\n|\\r", "\\n");
        
        return text.trim();
    }
    
    /**
     * 将文本分割为句子
     */
    private String[] splitIntoSentences(String text) {
        // 中英文句子分割规则
        String sentencePattern = "[.!?。！？；;]\\s*";
        String[] sentences = text.split(sentencePattern);
        
        // 过滤空句子
        return java.util.Arrays.stream(sentences)
                .filter(s -> s.trim().length() > 0)
                .toArray(String[]::new);
    }
    
    /**
     * 估算token数量（简化版本）
     * 实际项目中应使用专门的tokenizer
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) return 0;
        
        // 简化估算：中文字符按1个token计算，英文单词按平均4个字符1个token计算
        int chineseChars = 0;
        int englishChars = 0;
        
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseChars++;
            } else if (Character.isLetterOrDigit(c)) {
                englishChars++;
            }
        }
        
        return chineseChars + (englishChars / 4) + (text.split("\\s+").length / 2);
    }
    
    /**
     * 创建知识块
     */
    private void createChunk(List<KbChunk> chunks, String content, Long documentId, int chunkIndex, int tokenCount) {
        KbChunk chunk = new KbChunk();
        chunk.setDocumentId(documentId);
        chunk.setChapterName("块-" + chunkIndex);
        chunk.setChapter("块-" + chunkIndex);
        chunk.setContent(content.trim());
        chunk.setTokenCount(tokenCount);
        chunk.setSortOrder(chunkIndex - 1);
        chunk.setCreateTime(LocalDateTime.now());
        chunk.setUpdateTime(LocalDateTime.now());
        
        chunks.add(chunk);
        
        System.out.println("创建知识块 " + chunkIndex + ": tokens=" + tokenCount + ", 内容长度=" + content.length());
    }
}