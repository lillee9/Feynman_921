package com.dpsk.dpsk_quiz_sys_java.controller;


import com.dpsk.dpsk_quiz_sys_java.pojo.AiResult;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.QuestionResponse;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ResponseRecord;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import com.dpsk.dpsk_quiz_sys_java.repository.KbChunkRepository;
import com.dpsk.dpsk_quiz_sys_java.repository.KbDocumentRepository;
import com.dpsk.dpsk_quiz_sys_java.service.BaiduOcrService;
import com.dpsk.dpsk_quiz_sys_java.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin()
@RequestMapping("/question")
public class QuestionGeneratorController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionGeneratorController.class);

    private static final String DONE = "[DONE]";
    private static final Integer timeout = 120; // 增加超时时间到120秒
    private static final String AI_URL = "https://api.deepseek.com/chat/completions";

    @Value("${api.password:}")
    private String apiPassword;

    @Autowired
    private BaiduOcrService ocrService;
    
    @Autowired
    private KbChunkRepository kbChunkRepository;
    
    @Autowired
    private KbDocumentRepository kbDocumentRepository;

    /**
     * 根据图片内容生成题目
     * @param image 上传的图片
     * @param questionType 题目类型：CHOICE(选择题), SHORT_ANSWER(简答题), FILL_BLANK(填空题)
     * @param questionCount 生成题目数量，默认5道
     * @param difficulty 难度等级：EASY(简单), MEDIUM(中等), HARD(困难)
     * @param response HTTP响应
     */
    @PostMapping("/generate")
    public void generateQuestions(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "questionType", defaultValue = "CHOICE") String questionType,
            @RequestParam(value = "questionCount", defaultValue = "5") Integer questionCount,
            @RequestParam(value = "difficulty", defaultValue = "MEDIUM") String difficulty,
            HttpServletResponse response) throws IOException {

        // 1. 参数验证
        if (!isValidQuestionType(questionType)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("不支持的题目类型");
            return;
        }

        if (questionCount <= 0 || questionCount > 20) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("题目数量必须在1-20之间");
            return;
        }

        // 2. OCR识别
        String ocrText = processOcr(image);
        if (ocrText == null || ocrText.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("OCR识别失败或未识别到文本");
            return;
        }

        // 3. 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 4. 记录请求信息
        logQuestionGenerateRequest(ocrText, questionType, questionCount, difficulty);

        // 5. 调用AI生成题目
        try (PrintWriter pw = response.getWriter()) {
            generateQuestionsWithAI(pw, ocrText, questionType, questionCount, difficulty);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("生成题目过程中发生错误", e);
            throw new RuntimeException("生成题目过程中发生错误", e);
        }
    }

    /**
     * 批量生成混合类型题目
     */
    @PostMapping("/generate-mixed")
    public void generateMixedQuestions(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "choiceCount", defaultValue = "3") Integer choiceCount,
            @RequestParam(value = "shortAnswerCount", defaultValue = "2") Integer shortAnswerCount,
            @RequestParam(value = "fillBlankCount", defaultValue = "2") Integer fillBlankCount,
            @RequestParam(value = "difficulty", defaultValue = "MEDIUM") String difficulty,
            HttpServletResponse response) throws IOException {

        // OCR识别
        String ocrText = processOcr(image);
        if (ocrText == null || ocrText.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("OCR识别失败或未识别到文本");
            return;
        }

        // 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        try (PrintWriter pw = response.getWriter()) {
            // 分别生成不同类型的题目
            if (choiceCount > 0) {
                generateQuestionsWithAI(pw, ocrText, "CHOICE", choiceCount, difficulty);
            }
            if (shortAnswerCount > 0) {
                generateQuestionsWithAI(pw, ocrText, "SHORT_ANSWER", shortAnswerCount, difficulty);
            }
            if (fillBlankCount > 0) {
                generateQuestionsWithAI(pw, ocrText, "FILL_BLANK", fillBlankCount, difficulty);
            }

            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("生成混合题目过程中发生错误", e);
            throw new RuntimeException("生成混合题目过程中发生错误", e);
        }
    }

    /**
     * 基于知识库生成题目
     */
    @PostMapping("/generate-from-knowledge")
    public void generateFromKnowledge(
            @RequestBody Map<String, Object> request,
            HttpServletResponse response) throws IOException {

        // 1. 解析请求参数
        @SuppressWarnings("unchecked")
        List<Object> documentIdsRaw = (List<Object>) request.get("documentIds");
        List<Long> documentIds = documentIdsRaw != null ? 
            documentIdsRaw.stream().map(id -> Long.valueOf(id.toString())).collect(java.util.stream.Collectors.toList()) : 
            null;
        @SuppressWarnings("unchecked")
        List<String> chapters = (List<String>) request.get("chapters");
        String questionType = (String) request.get("questionType");
        Integer questionCount = (Integer) request.get("questionCount");
        String difficulty = (String) request.get("difficulty");

        if (documentIds == null || documentIds.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请选择知识库文档");
            return;
        }

        // 2. 构建知识库内容提示词
        String knowledgeContent = buildKnowledgePrompt(documentIds, chapters);
        if (knowledgeContent == null || knowledgeContent.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("未找到相关知识库内容");
            return;
        }

        // 3. 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 4. 记录请求信息
        logQuestionGenerateRequest(knowledgeContent, questionType, questionCount, difficulty);

        // 5. 调用AI生成题目
        try (PrintWriter pw = response.getWriter()) {
            generateQuestionsWithAI(pw, knowledgeContent, questionType, questionCount, difficulty);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("基于知识库生成题目过程中发生错误", e);
            throw new RuntimeException("基于知识库生成题目过程中发生错误", e);
        }
    }

    /**
     * 基于文档生成题目（支持Word、PDF、图片等多种格式）
     */
    @PostMapping("/generate-from-document")
    @CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, allowCredentials = "true")
    public void generateFromDocument(
            @RequestParam("document") MultipartFile document,
            @RequestParam(value = "questionType", defaultValue = "CHOICE") String questionType,
            @RequestParam(value = "questionCount", defaultValue = "5") Integer questionCount,
            @RequestParam(value = "difficulty", defaultValue = "MEDIUM") String difficulty,
            HttpServletResponse response) throws IOException {

        // 1. 参数验证
        if (!isValidQuestionType(questionType)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("不支持的题目类型");
            return;
        }

        if (questionCount <= 0 || questionCount > 20) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("题目数量必须在1-20之间");
            return;
        }

        // 2. 统一文件处理（文本提取或OCR识别）
        String extractedText = processOcr(document);
        logger.info("文档处理结果 - 文件名: {}, 提取文本长度: {}, 文本预览: {}", 
                   document.getOriginalFilename(), 
                   extractedText != null ? extractedText.length() : 0,
                   extractedText != null && extractedText.length() > 100 ? 
                   extractedText.substring(0, 100) + "..." : extractedText);
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            logger.error("文档处理失败 - 文件名: {}, 提取文本为空", document.getOriginalFilename());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("文件处理失败或未提取到文本内容");
            return;
        }

        // 3. 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 4. 记录请求信息
        logQuestionGenerateRequest(extractedText, questionType, questionCount, difficulty);

        // 5. 调用AI生成题目
        try (PrintWriter pw = response.getWriter()) {
            generateQuestionsWithAI(pw, extractedText, questionType, questionCount, difficulty);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("基于文档生成题目过程中发生错误", e);
            throw new RuntimeException("基于文档生成题目过程中发生错误", e);
        }
    }

    /**
     * 基于AI直接生成题目
     */
    @PostMapping("/generate-ai")
    public void generateWithAI(
            @RequestBody Map<String, Object> request,
            HttpServletResponse response) throws IOException {

        // 1. 解析请求参数
        String topic = (String) request.get("topic");
        String requirements = (String) request.get("requirements");
        String questionType = (String) request.get("questionType");
        Integer questionCount = (Integer) request.get("questionCount");
        String difficulty = (String) request.get("difficulty");

        if (topic == null || topic.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请输入题目主题");
            return;
        }

        // 2. 构建AI生成提示词
        String aiContent = buildAIPrompt(topic, requirements);

        // 3. 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 4. 记录请求信息
        logQuestionGenerateRequest(aiContent, questionType, questionCount, difficulty);

        // 5. 调用AI生成题目
        try (PrintWriter pw = response.getWriter()) {
            generateQuestionsWithAI(pw, aiContent, questionType, questionCount, difficulty);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("AI直接生成题目过程中发生错误", e);
            throw new RuntimeException("AI直接生成题目过程中发生错误", e);
        }
    }

    /**
     * 统一文件处理入口 - 根据文件类型选择处理策略
     * 文本格式（txt、word）→ 直接文本提取
     * 复杂格式（图片、pdf、ppt）→ OCR识别
     */
    private String processOcr(MultipartFile image) {
        String fileName = image.getOriginalFilename();
        if (fileName == null) {
            logger.error("文件名为空");
            return null;
        }
        
        String extension = fileName.toLowerCase();
        logger.info("处理文件: {}, 扩展名: {}", fileName, extension);
        
        try {
            // 1. 文本格式 - 直接文本提取
            if (isTextFormat(extension)) {
                return processTextFile(image, extension);
            }
            // 2. 复杂格式 - OCR识别
            else if (isOcrFormat(extension)) {
                return processOcrFile(image);
            }
            else {
                logger.error("不支持的文件格式: {}", extension);
                return null;
            }
        } catch (Exception e) {
            logger.error("文件处理失败: {}", fileName, e);
            return null;
        }
    }
    
    /**
     * 判断是否为文本格式
     */
    private boolean isTextFormat(String fileName) {
        return fileName.endsWith(".txt") || fileName.endsWith(".doc") || fileName.endsWith(".docx");
    }
    
    /**
     * 判断是否为OCR格式
     */
    private boolean isOcrFormat(String fileName) {
        return fileName.endsWith(".pdf") || fileName.endsWith(".png") || 
               fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".ppt") || fileName.endsWith(".pptx");
    }
    
    /**
     * 处理文本格式文件
     */
    private String processTextFile(MultipartFile file, String extension) throws Exception {
        logger.info("开始处理文本文件: {}, 扩展名: {}, 文件大小: {} bytes", file.getOriginalFilename(), extension, file.getSize());
        
        if (extension.endsWith(".txt")) {
            String content = new String(file.getBytes(), "UTF-8");
            logger.info("直接读取txt文件内容成功，长度: {}", content.length());
            return content;
        }
        else if (extension.endsWith(".doc") || extension.endsWith(".docx")) {
            logger.info("开始解析Word文档: {}", file.getOriginalFilename());
            String wordContent = parseWordDocument(file);
            logger.info("Word文档解析完成，内容长度: {}", wordContent != null ? wordContent.length() : 0);
            return wordContent;
        }
        logger.warn("不支持的文本文件格式: {}", extension);
        return null;
    }
    
    /**
     * 解析Word文档内容
     */
    private String parseWordDocument(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new Exception("文件名为空");
        }
        
        // 验证文件大小
        if (file.getSize() == 0) {
            throw new Exception("文件为空");
        }
        
        // 验证文件大小不超过50MB
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new Exception("文件过大，请上传小于50MB的文件");
        }
        
        // 先读取文件头进行验证
        byte[] header = new byte[8];
        try (InputStream headerStream = file.getInputStream()) {
            int bytesRead = headerStream.read(header);
            if (bytesRead < 4) {
                throw new Exception("文件格式无效：文件头读取失败");
            }
        }
        
        // 重新获取输入流进行文档解析
        try (InputStream inputStream = file.getInputStream()) {
            String content;
            
            if (fileName.toLowerCase().endsWith(".docx")) {
                // 验证.docx文件头（ZIP格式：PK）
                if (header[0] != 0x50 || header[1] != 0x4B) {
                    throw new Exception("文件格式错误：不是有效的.docx文件");
                }
                
                // 处理 .docx 文件
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    content = extractor.getText();
                }
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                // 验证.doc文件头（OLE格式）
                if (header[0] != (byte)0xD0 || header[1] != (byte)0xCF || 
                    header[2] != (byte)0x11 || header[3] != (byte)0xE0) {
                    throw new Exception("文件格式错误：不是有效的.doc文件");
                }
                
                // 处理 .doc 文件
                try (HWPFDocument document = new HWPFDocument(inputStream);
                     WordExtractor extractor = new WordExtractor(document)) {
                    content = extractor.getText();
                }
            } else {
                throw new Exception("不支持的Word文档格式: " + fileName);
            }
            
            if (content == null || content.trim().isEmpty()) {
                throw new Exception("Word文档内容为空或无法提取文本内容");
            }
            
            logger.info("Word文档解析成功，文件: {}, 内容长度: {}", fileName, content.length());
            return content.trim();
            
        } catch (Exception e) {
            logger.error("Word文档解析失败: {}", fileName, e);
            
            // 检查是否是依赖问题
            if (e.getCause() instanceof NoClassDefFoundError || 
                e.getMessage() != null && e.getMessage().contains("NoClassDefFoundError")) {
                throw new Exception("系统依赖库缺失，请联系管理员检查Apache POI相关依赖配置");
            }
            
            // 检查是否是文件格式问题
            if (e.getMessage() != null && (e.getMessage().contains("格式错误") || 
                e.getMessage().contains("FileMagic") || 
                e.getMessage().contains("Invalid header signature") ||
                e.getMessage().contains("Your file appears not to be a valid"))) {
                throw new Exception("文件格式错误，请确保上传的是有效的Word文档(.doc或.docx)，建议重新保存文件后再上传");
            }
            
            // 检查是否是文件损坏问题
            if (e.getMessage() != null && (e.getMessage().contains("corrupted") || 
                e.getMessage().contains("damaged") ||
                e.getMessage().contains("unexpected end"))) {
                throw new Exception("文件可能已损坏，请检查文件完整性或重新生成文档");
            }
            
            // 检查是否是内存问题
            if (e.getCause() instanceof OutOfMemoryError || 
                e.getMessage() != null && e.getMessage().contains("OutOfMemoryError")) {
                throw new Exception("文件过大导致内存不足，请尝试上传较小的文件");
            }
            
            // 通用错误处理
            String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
            throw new Exception("Word文档解析失败: " + errorMsg + "。建议检查文件格式或联系技术支持", e);
        }
    }
    
    /**
     * 处理OCR格式文件
     */
    private String processOcrFile(MultipartFile file) throws Exception {
        String ocrResult = ocrService.recognizeImg(file);
        return integrateOcrResults(ocrResult);
    }

    private String integrateOcrResults(String ocrJson) {
        try {
            JsonNode rootNode = JsonUtils.getObjectMapper().readTree(ocrJson);
            JsonNode wordsResults = rootNode.path("words_result");

            StringBuilder integratedText = new StringBuilder();
            for (JsonNode item : wordsResults) {
                integratedText.append(item.path("words").asText()).append(" ");
            }

            return integratedText.toString().trim();
        } catch (IOException e) {
            logger.error("OCR结果解析失败", e);
            return ocrJson;
        }
    }

    private void generateQuestionsWithAI(PrintWriter pw, String content, String questionType,
                                         Integer questionCount, String difficulty) throws InterruptedException {

        // 构造对话消息
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示 - 根据题目类型定制
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(questionType, difficulty));
        messages.add(systemMessage);

        // 用户问题
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", buildUserPrompt(content, questionType, questionCount));
        messages.add(userMessage);

        // 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("model", "deepseek-chat");
        params.put("stream", true);
        params.put("messages", messages);

        String jsonParams = JsonUtils.convertObj2Json(params);

        // 创建请求
        Request.Builder builder = new Request.Builder().url(AI_URL);
        builder.addHeader("Authorization", "Bearer " + apiPassword);
        builder.addHeader("Accept", "text/event-stream");

        okhttp3.RequestBody body = okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) // 启用连接失败重试
                .build();
        
        logger.info("开始调用Deepseek API，超时设置: {}秒", timeout);

        CountDownLatch eventLatch = new CountDownLatch(1);
        List<ResponseRecord> aiResponses = new ArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (DONE.equals(data)) {
                    return;
                }
                String content = getContent(data);
                if (content != null && !content.isEmpty()) {
                    fullResponse.append(content);

                    // 记录AI响应
                    aiResponses.add(new ResponseRecord(
                            id,
                            ResponseRecord.TYPE_AI_RESPONSE,
                            System.currentTimeMillis(),
                            content,
                            data
                    ));

//                    // 发送流式数据给前端
//                    QuestionResponse questionResponse = new QuestionResponse();
//                    questionResponse.setType(questionType);
//                    questionResponse.setContent(content);
//                    questionResponse.setStreamData(true);
//
//                    pw.write("data:" + JsonUtils.convertObj2Json(questionResponse) + "\n\n");
//                    pw.flush();
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);

                try {
                    logger.info("AI响应完成，开始解析。完整响应长度: {}", fullResponse.length());
                    logger.debug("AI完整响应内容: {}", fullResponse.toString());

                    // 解析 AI 响应文本
                    List<QuestionResponse> parsedQuestions = parseAIResponse(fullResponse.toString(), questionType);

                    if (parsedQuestions == null || parsedQuestions.isEmpty()) {
                        logger.error("解析AI响应后没有获得有效题目");
                        pw.write("data:{\"error\":\"没有生成有效的题目\"}\n\n");
                        pw.flush();
                        return;
                    }

                    logger.info("成功解析出 {} 道题目", parsedQuestions.size());

                    // 发送每个完整的题目（逐个发送，便于前端实时显示）
                    for (int i = 0; i < parsedQuestions.size(); i++) {
                        QuestionResponse question = parsedQuestions.get(i);
                        question.setId(i + 1); // 设置题目编号
                        question.setStreamData(false); // 标记为完整数据

                        String questionJson = JsonUtils.convertObj2Json(question);
                        pw.write("data:" + questionJson + "\n\n");
                        pw.flush();

                        // 小延迟，让前端有时间处理
                        Thread.sleep(100);
                    }

                    // 最后发送汇总信息
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("type", "summary");
                    summary.put("total", parsedQuestions.size());
                    summary.put("completed", true);
                    summary.put("questionType", questionType);
                    summary.put("difficulty", difficulty);

                    String summaryJson = JsonUtils.convertObj2Json(summary);
                    pw.write("data:" + summaryJson + "\n\n");
                    pw.flush();

                } catch (Exception e) {
                    logger.error("解析AI响应失败", e);
                    try {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", "解析AI响应失败: " + e.getMessage());
                        error.put("type", "error");

                        pw.write("data:" + JsonUtils.convertObj2Json(error) + "\n\n");
                        pw.flush();
                    } catch (Exception ex) {
                        logger.error("发送错误信息也失败了", ex);
                    }
                }

                // 记录日志
                logAiResponse(aiResponses, questionType);

                // 通知完成
                eventLatch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorType = t.getClass().getSimpleName();
                logger.error("调用Deepseek API失败，错误类型: {}, 错误信息: {}", errorType, t.getMessage(), t);
                
                // 记录响应状态码（如果有）
                if (response != null) {
                    logger.error("HTTP响应状态码: {}", response.code());
                }

                try {
                    Map<String, Object> error = new HashMap<>();
                    
                    // 根据错误类型提供更友好的错误信息
                    if (t instanceof java.net.SocketTimeoutException) {
                        error.put("error", "AI服务响应超时，请稍后重试。如果问题持续存在，请尝试减少题目数量或简化内容。");
                        error.put("errorCode", "TIMEOUT");
                    } else if (t instanceof java.net.ConnectException) {
                        error.put("error", "无法连接到AI服务，请检查网络连接后重试。");
                        error.put("errorCode", "CONNECTION_FAILED");
                    } else {
                        error.put("error", "AI服务调用失败: " + t.getMessage());
                        error.put("errorCode", "API_ERROR");
                    }
                    
                    error.put("type", "error");
                    error.put("timestamp", System.currentTimeMillis());

                    pw.write("data:" + JsonUtils.convertObj2Json(error) + "\n\n");
                    pw.flush();
                } catch (Exception e) {
                    logger.error("发送API失败错误信息失败", e);
                }

                if (eventLatch != null) {
                    eventLatch.countDown();
                }
            }
        });

        realEventSource.connect(client);
        eventLatch.await();
    }

    private String buildSystemPrompt(String questionType, String difficulty) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的贷后催收培训题目生成专家，能够根据给定的催收相关文本内容生成高质量的培训题目。题目应贴合催收业务实际场景，涵盖催收话术、法律法规、风险控制、客户心理分析等方面。");

        switch (questionType) {
            case "CHOICE":
            case "SINGLE_CHOICE":
                prompt.append("请生成单选题，每道题包含题目、4个选项(A/B/C/D)和正确答案。");
                break;
            case "MULTI_CHOICE":
                prompt.append("请生成多选题，每道题包含题目、4个选项(A/B/C/D)和正确答案(可以是多个选项)。");
                break;
            case "SHORT_ANSWER":
                prompt.append("请生成简答题，每道题包含题目和详细的参考答案。");
                break;
            case "FILL_BLANK":
                prompt.append("请生成填空题，每道题包含带有空格的题目和正确答案。");
                break;
        }

        prompt.append("难度等级：").append(difficulty);
        prompt.append("。请确保题目内容准确、逻辑清晰，答案正确。");
        prompt.append("\n\n【重要】请严格按照以下JSON格式返回，不要添加任何markdown代码块标记或其他格式：");
        prompt.append(getJsonFormatExample(questionType));
        prompt.append("\n\n请直接返回JSON格式的数据，不要包含任何解释文字或代码块标记。");

        return prompt.toString();
    }

    private String buildUserPrompt(String content, String questionType, Integer questionCount) {
        return String.format("基于以下内容生成%d道%s题目，请直接返回JSON格式数据：\n\n%s",
                questionCount, getQuestionTypeName(questionType), content);
    }

    private String getQuestionTypeName(String questionType) {
        switch (questionType) {
            case "CHOICE":
            case "SINGLE_CHOICE": return "单选";
            case "MULTI_CHOICE": return "多选";
            case "SHORT_ANSWER": return "简答";
            case "FILL_BLANK": return "填空";
            default: return "综合";
        }
    }

    private String getJsonFormatExample(String questionType) {
        switch (questionType) {
            case "CHOICE":
            case "SINGLE_CHOICE":
                return "\n{\"questions\":[{\"id\":1,\"type\":\"SINGLE_CHOICE\",\"question\":\"题目内容\",\"options\":[\"选项1\",\"选项2\",\"选项3\",\"选项4\"],\"answer\":\"A\",\"explanation\":\"解析\"}]}";
            case "MULTI_CHOICE":
                return "\n{\"questions\":[{\"id\":1,\"type\":\"MULTI_CHOICE\",\"question\":\"题目内容\",\"options\":[\"选项1\",\"选项2\",\"选项3\",\"选项4\"],\"answer\":\"AB\",\"explanation\":\"解析\"}]}";
            case "SHORT_ANSWER":
                return "\n{\"questions\":[{\"id\":1,\"type\":\"SHORT_ANSWER\",\"question\":\"题目内容\",\"answer\":\"参考答案\",\"keyPoints\":[\"要点1\",\"要点2\"]}]}";
            case "FILL_BLANK":
                return "\n{\"questions\":[{\"id\":1,\"type\":\"FILL_BLANK\",\"question\":\"题目内容，其中____表示需要填空的地方\",\"answer\":\"正确答案\",\"explanation\":\"解析\"}]}";
            default:
                return "";
        }
    }

    private List<QuestionResponse> parseAIResponse(String aiResponse, String questionType) {
        List<QuestionResponse> questions = new ArrayList<>();

        // 预处理AI响应，提取JSON部分
        String cleanedResponse = cleanAIResponse(aiResponse);

        try {
            // 尝试解析JSON格式的响应
            JsonNode rootNode = JsonUtils.getObjectMapper().readTree(cleanedResponse);
            JsonNode questionsNode = rootNode.path("questions");

            for (JsonNode questionNode : questionsNode) {
                QuestionResponse question = new QuestionResponse();
                question.setId(questionNode.path("id").asInt());
                question.setType(questionType);
                question.setQuestion(questionNode.path("question").asText());
                String answer = questionNode.path("answer").asText();
                question.setAnswer(answer);
                question.setCorrectAnswer(answer);
                question.setStreamData(false);

                // 根据题目类型设置特定字段
                if ("CHOICE".equals(questionType) || "SINGLE_CHOICE".equals(questionType) || "MULTI_CHOICE".equals(questionType)) {
                    List<String> options = new ArrayList<>();
                    JsonNode optionsNode = questionNode.path("options");
                    for (JsonNode option : optionsNode) {
                        options.add(option.asText());
                    }
                    question.setOptions(options);
                    question.setExplanation(questionNode.path("explanation").asText());
                } else if ("SHORT_ANSWER".equals(questionType)) {
                    List<String> keyPoints = new ArrayList<>();
                    JsonNode keyPointsNode = questionNode.path("keyPoints");
                    for (JsonNode keyPoint : keyPointsNode) {
                        keyPoints.add(keyPoint.asText());
                    }
                    question.setKeyPoints(keyPoints);
                } else if ("FILL_BLANK".equals(questionType)) {
                    question.setExplanation(questionNode.path("explanation").asText());
                }

                questions.add(question);
            }
        } catch (Exception e) {
            logger.error("解析AI响应JSON失败，尝试文本解析", e);
            logger.debug("原始AI响应内容: {}", aiResponse);
            logger.debug("清理后的响应内容: {}", cleanedResponse);

            // 如果JSON解析失败，尝试文本解析
            questions.addAll(parseTextResponse(aiResponse, questionType));
        }

        return questions;
    }

    /**
     * 清理AI响应，提取JSON部分
     */
    private String cleanAIResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        // 移除markdown代码块标记
        String cleaned = response.trim();

        // 查找JSON代码块
        if (cleaned.contains("```json")) {
            int jsonStart = cleaned.indexOf("```json") + 7;
            int jsonEnd = cleaned.indexOf("```", jsonStart);
            if (jsonEnd > jsonStart) {
                cleaned = cleaned.substring(jsonStart, jsonEnd).trim();
            }
        } else if (cleaned.contains("```")) {
            // 处理普通代码块
            int blockStart = cleaned.indexOf("```");
            int blockEnd = cleaned.indexOf("```", blockStart + 3);
            if (blockEnd > blockStart) {
                String codeBlock = cleaned.substring(blockStart + 3, blockEnd);
                // 检查代码块是否包含JSON
                if (codeBlock.trim().startsWith("{") || codeBlock.trim().startsWith("[")) {
                    cleaned = codeBlock.trim();
                }
            }
        }

        // 查找第一个 { 和最后一个 } 之间的内容
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }

    /**
     * 文本解析方式解析AI响应
     */
    private List<QuestionResponse> parseTextResponse(String response, String questionType) {
        List<QuestionResponse> questions = new ArrayList<>();

        try {
            // 按行分割响应
            String[] lines = response.split("\n");
            QuestionResponse currentQuestion = null;
            int questionId = 1;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 检测新题目开始的标志
                if (isQuestionStart(line, questionType)) {
                    if (currentQuestion != null) {
                        questions.add(currentQuestion);
                    }
                    currentQuestion = new QuestionResponse();
                    currentQuestion.setId(questionId++);
                    currentQuestion.setType(questionType);
                    currentQuestion.setStreamData(false);

                    // 提取题目内容
                    String questionText = extractQuestionText(line);
                    currentQuestion.setQuestion(questionText);
                }
                // 提取选项（选择题）
                else if ("CHOICE".equals(questionType) && isOption(line)) {
                    if (currentQuestion != null) {
                        if (currentQuestion.getOptions() == null) {
                            currentQuestion.setOptions(new ArrayList<>());
                        }
                        currentQuestion.getOptions().add(line);
                    }
                }
                // 提取答案
                else if (isAnswer(line)) {
                    if (currentQuestion != null) {
                        String answer = extractAnswer(line);
                        currentQuestion.setAnswer(answer);
                        currentQuestion.setCorrectAnswer(answer);
                    }
                }
                // 提取解析
                else if (isExplanation(line)) {
                    if (currentQuestion != null) {
                        String explanation = extractExplanation(line);
                        currentQuestion.setExplanation(explanation);
                    }
                }
            }

            // 添加最后一个题目
            if (currentQuestion != null) {
                questions.add(currentQuestion);
            }

        } catch (Exception e) {
            logger.error("文本解析失败", e);
            // 创建一个包含原始内容的备用题目
            questions.add(createFallbackQuestion(response, questionType));
        }

        return questions;
    }

    private QuestionResponse createFallbackQuestion(String content, String questionType) {
        QuestionResponse question = new QuestionResponse();
        question.setId(1);
        question.setType(questionType);
        question.setQuestion("AI生成的题目内容");
        question.setAnswer(content);
        question.setCorrectAnswer(content);
        question.setStreamData(false);

        // 为不同类型的题目提供默认结构
        switch (questionType) {
            case "CHOICE":
                question.setOptions(Arrays.asList("选项1", "选项2", "选项3", "选项4"));
                question.setExplanation("请参考AI生成的完整内容");
                break;
            case "SHORT_ANSWER":
                question.setKeyPoints(Arrays.asList("关键要点请参考AI生成内容"));
                break;
            case "FILL_BLANK":
                question.setExplanation("请参考AI生成的完整内容");
                break;
        }

        return question;
    }

    private boolean isQuestionStart(String line, String questionType) {
        // 常见的题目开始标志
        return line.matches("^\\d+[.、].*") ||  // 数字开头
                line.startsWith("题目") ||
                line.startsWith("问题") ||
                line.contains("下列") ||
                line.contains("以下");
    }

    private String extractQuestionText(String line) {
        // 移除题号
        return line.replaceFirst("^\\d+[.、]\\s*", "").trim();
    }

    private boolean isOption(String line) {
        // 选项标志：A. B. C. D. 或 A、B、C、D、
        return line.matches("^[A-D][.、].*");
    }

    private boolean isAnswer(String line) {
        return line.startsWith("答案") ||
                line.startsWith("正确答案") ||
                line.startsWith("参考答案") ||
                line.contains("答案：") ||
                line.contains("答案:");
    }

    private String extractAnswer(String line) {
        return line.replaceFirst("^(答案|正确答案|参考答案)[：:]?\\s*", "").trim();
    }

    private boolean isExplanation(String line) {
        return line.startsWith("解析") ||
                line.startsWith("解释") ||
                line.contains("解析：") ||
                line.contains("解析:");
    }

    private String extractExplanation(String line) {
        return line.replaceFirst("^(解析|解释)[：:]?\\s*", "").trim();
    }

    private boolean isValidQuestionType(String questionType) {
        return Arrays.asList("CHOICE", "SINGLE_CHOICE", "MULTI_CHOICE", "SHORT_ANSWER", "FILL_BLANK").contains(questionType);
    }

    private static String getContent(String data) {
        try {
            AiResult aiResult = JsonUtils.convertJson2Obj(data, AiResult.class);
            return aiResult.getChoices().get(0).getDelta().getContent();
        } catch (Exception e) {
            return null;
        }
    }

    private void logQuestionGenerateRequest(String ocrText, String questionType, Integer questionCount, String difficulty) {
        logger.info("\n===== 题目生成请求 =====\n" +
                        "OCR内容: {}\n" +
                        "题目类型: {}\n" +
                        "题目数量: {}\n" +
                        "难度等级: {}\n",
                ocrText.replaceAll("\\s+", " ").trim(),
                questionType,
                questionCount,
                difficulty);
    }

    private void logAiResponse(List<ResponseRecord> aiResponses, String questionType) {
        String fullContent = aiResponses.stream()
                .map(ResponseRecord::getContent)
                .collect(Collectors.joining());

        logger.info("\n===== AI生成{}题目完整响应 =====\n{}\n", getQuestionTypeName(questionType), fullContent);
    }

    /**
     * 调试端点：检查知识块数据
     */
    @GetMapping("/debug/chunks")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> debugChunks(
            @RequestParam(required = false) List<Long> documentIds,
            @RequestParam(required = false) List<String> chapters) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 查询所有知识块总数
            long totalChunks = kbChunkRepository.count();
            debugInfo.put("totalChunks", totalChunks);
            
            // 查询所有文档ID
            List<KbChunk> allChunks = kbChunkRepository.findAll();
            Set<Long> allDocumentIds = allChunks.stream()
                .map(KbChunk::getDocumentId)
                .collect(java.util.stream.Collectors.toSet());
            debugInfo.put("availableDocumentIds", allDocumentIds);
            
            // 查询所有章节名称
            Set<String> allChapterNames = allChunks.stream()
                .map(KbChunk::getChapterName)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
            debugInfo.put("availableChapterNames", allChapterNames);
            
            // 如果指定了文档ID，查询这些文档的知识块
            if (documentIds != null && !documentIds.isEmpty()) {
                List<KbChunk> docChunks = new ArrayList<>();
                for (Long docId : documentIds) {
                    docChunks.addAll(kbChunkRepository.findByDocumentId(docId));
                }
                debugInfo.put("requestedDocumentChunks", docChunks.size());
                debugInfo.put("sampleChunks", docChunks.stream()
                    .limit(3)
                    .map(chunk -> Map.of(
                        "chunkId", chunk.getChunkId(),
                        "documentId", chunk.getDocumentId(),
                        "chapterName", chunk.getChapterName() != null ? chunk.getChapterName() : "null",
                        "contentPreview", chunk.getContent().length() > 100 ? 
                            chunk.getContent().substring(0, 100) + "..." : chunk.getContent()
                    ))
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            // 如果指定了章节，查询这些章节的知识块
            if (chapters != null && !chapters.isEmpty()) {
                List<KbChunk> chapterChunks = kbChunkRepository.findByChapterNameIn(chapters);
                debugInfo.put("requestedChapterChunks", chapterChunks.size());
            }
            
            logger.info("调试信息: {}", debugInfo);
            return ResponseEntity.ok(ResponseMessage.success(debugInfo));
            
        } catch (Exception e) {
            logger.error("调试端点执行失败", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "调试失败: " + e.getMessage());
            return ResponseEntity.ok(ResponseMessage.error(errorInfo));
        }
    }

    /**
     * 构建知识库内容提示词
     */
    private String buildKnowledgePrompt(List<Long> documentIds, List<String> chapters) {
        StringBuilder content = new StringBuilder();
        
        try {
            logger.info("=== 开始构建知识库提示词 ===");
            logger.info("接收到的参数 - 文档ID: {}, 章节: {}", documentIds, chapters);
            
            // 参数验证
            if (documentIds == null || documentIds.isEmpty()) {
                logger.error("参数验证失败: documentIds为空或null");
                return null;
            }
            logger.info("参数验证通过，文档ID数量: {}", documentIds.size());
            
            List<KbChunk> chunks;
            
            if (chapters != null && !chapters.isEmpty()) {
                // 如果指定了章节，按章节查找
                logger.info("=== 按章节查找知识块 ===");
                logger.info("查询章节列表: {}", chapters);
                logger.info("执行SQL查询: findByChapterNameIn({})", chapters);
                
                chunks = kbChunkRepository.findByChapterNameIn(chapters);
                logger.info("章节查询结果: 找到{}个知识块", chunks.size());
                
                // 记录查询到的章节详情
                if (!chunks.isEmpty()) {
                    Map<String, Long> chapterCounts = chunks.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            chunk -> chunk.getChapterName() != null ? chunk.getChapterName() : "null",
                            java.util.stream.Collectors.counting()));
                    logger.info("各章节知识块分布: {}", chapterCounts);
                }
                
                // 过滤指定文档的章节
                logger.info("开始过滤文档ID: {}", documentIds);
                List<KbChunk> beforeFilter = new ArrayList<>(chunks);
                chunks = chunks.stream()
                    .filter(chunk -> documentIds.contains(chunk.getDocumentId()))
                    .collect(java.util.stream.Collectors.toList());
                logger.info("过滤结果: {}个 -> {}个知识块", beforeFilter.size(), chunks.size());
                
                // 记录被过滤掉的文档ID
                Set<Long> filteredDocIds = beforeFilter.stream()
                    .map(KbChunk::getDocumentId)
                    .filter(docId -> !documentIds.contains(docId))
                    .collect(java.util.stream.Collectors.toSet());
                if (!filteredDocIds.isEmpty()) {
                    logger.info("被过滤的文档ID: {}", filteredDocIds);
                }
            } else {
                // 如果没有指定章节，查找指定文档的所有知识块
                logger.info("=== 按文档ID查找所有知识块 ===");
                logger.info("查询文档ID列表: {}", documentIds);
                
                chunks = new java.util.ArrayList<>();
                for (Long documentId : documentIds) {
                    logger.info("执行SQL查询: findByDocumentId({})", documentId);
                    List<KbChunk> docChunks = kbChunkRepository.findByDocumentId(documentId);
                    logger.info("文档ID {} 查询结果: {}个知识块", documentId, docChunks.size());
                    
                    // 记录该文档的章节分布
                    if (!docChunks.isEmpty()) {
                        Set<String> docChapters = docChunks.stream()
                            .map(chunk -> chunk.getChapterName() != null ? chunk.getChapterName() : "null")
                            .collect(java.util.stream.Collectors.toSet());
                        logger.info("文档ID {} 包含章节: {}", documentId, docChapters);
                    }
                    
                    chunks.addAll(docChunks);
                }
                logger.info("=== 文档查询汇总 ===");
                logger.info("总共找到知识块数量: {}", chunks.size());
                
                // 统计所有章节分布
                if (!chunks.isEmpty()) {
                    Map<String, Long> allChapterCounts = chunks.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            chunk -> chunk.getChapterName() != null ? chunk.getChapterName() : "null",
                            java.util.stream.Collectors.counting()));
                    logger.info("所有文档章节分布: {}", allChapterCounts);
                }
            }
            
            if (chunks.isEmpty()) {
                logger.error("=== 未找到相关知识库内容 ===");
                logger.error("查询参数 - 文档ID: {}, 章节: {}", documentIds, chapters);
                
                // 进一步诊断问题
                logger.error("开始诊断数据库状态...");
                long totalChunks = kbChunkRepository.count();
                logger.error("数据库中总知识块数量: {}", totalChunks);
                
                if (totalChunks == 0) {
                    logger.error("诊断结果: 数据库中没有任何知识块数据");
                } else {
                    // 检查指定文档是否存在
                    for (Long docId : documentIds) {
                        long docChunkCount = kbChunkRepository.countByDocumentId(docId);
                        logger.error("文档ID {} 的知识块数量: {}", docId, docChunkCount);
                    }
                    
                    // 如果指定了章节，检查章节是否存在
                    if (chapters != null && !chapters.isEmpty()) {
                        for (String chapter : chapters) {
                            long chapterCount = kbChunkRepository.countByChapterName(chapter);
                            logger.error("章节 '{}' 的知识块数量: {}", chapter, chapterCount);
                        }
                    }
                }
                
                return null;
            }
            
            content.append("以下是相关知识库内容：\n\n");
            
            for (KbChunk chunk : chunks) {
                if (chunk.getChapterName() != null && !chunk.getChapterName().trim().isEmpty()) {
                    content.append("【章节：").append(chunk.getChapterName()).append("】\n");
                }
                content.append(chunk.getContent()).append("\n\n");
            }
            
            content.append("请根据以上知识库内容生成相关题目。");
            
            logger.info("=== 知识库提示词构建完成 ===");
            logger.info("包含知识块数量: {}", chunks.size());
            logger.info("提示词总长度: {} 字符", content.length());
            
            // 记录内容预览
            String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content.toString();
            logger.info("提示词内容预览: {}", preview.replaceAll("\n", "\\n"));
            
        } catch (Exception e) {
            logger.error("构建知识库提示词失败", e);
            return null;
        }
        
        return content.toString();
    }

    /**
     * 构建AI生成提示词
     */
    private String buildAIPrompt(String topic, String requirements) {
        StringBuilder content = new StringBuilder();
        content.append("题目主题: ").append(topic).append("\n\n");
        if (requirements != null && !requirements.trim().isEmpty()) {
            content.append("具体要求: ").append(requirements).append("\n\n");
        }
        content.append("请根据以上主题和要求生成相关题目。");
        return content.toString();
    }
}
