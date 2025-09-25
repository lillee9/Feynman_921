package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.*;
import com.dpsk.dpsk_quiz_sys_java.repository.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IAIService;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamPaperService;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class ExamPaperService implements IExamPaperService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamPaperService.class);
    
    @Autowired
    private ExamPaperRepository examPaperRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private KbChunkRepository kbChunkRepository;
    
    @Autowired
    private IAIService aiService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private IExamQuestionService examQuestionService;
    
    @Autowired
    private ExamAnswerRepository examAnswerRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // JSON字段最大长度限制（MySQL JSON类型默认最大4GB，但实际使用中建议限制在64KB以内）
    private static final int MAX_JSON_LENGTH = 65535; // 64KB
    
    @Override
    public ExamPaperResponse createPaperFromKnowledgeBase(ExamPaperCreateRequest request, Long creatorId) {
        validateCreateRequest(request, creatorId);
        
        // 创建试卷实体
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(request.getPaperName());
        paper.setCreatorId(creatorId);
        paper.setChapterScope(request.getChapterScope());
        paper.setQuestionConfig(request.getQuestionConfig());
        paper.setDifficulty(request.getDifficulty());
        paper.setTotalScore(request.getTotalScore());
        if (request.getTotalQuestion() != null) {
            paper.setTotalQuestion(request.getTotalQuestion());
        }
        paper.setExpireTime(request.getExpireTime());
        // generateTime在构造函数中自动设置
        paper.setStatus(ExamPaper.PaperStatus.published);
        
        // 验证和处理JSON字段
        validateAndProcessJsonFields(paper);
        
        // 保存试卷
        ExamPaper savedPaper = examPaperRepository.save(paper);
        
        // 基于知识库生成题目
        generateQuestionsFromKnowledgeBase(savedPaper, request);
        
        return convertToResponse(savedPaper);
    }
    
    /**
     * 验证和处理JSON字段长度
     */
    private void validateAndProcessJsonFields(ExamPaper paper) {
        try {
            // 处理chapterScope字段
            if (paper.getChapterScope() != null) {
                String chapterScopeJson = objectMapper.writeValueAsString(paper.getChapterScope());
                if (chapterScopeJson.length() > MAX_JSON_LENGTH) {
                    logger.warn("章节范围JSON数据过长，进行截断处理。原长度: {}, 限制长度: {}", 
                               chapterScopeJson.length(), MAX_JSON_LENGTH);
                    // 截断处理：保留前面的章节
                    List<String> truncatedChapterScope = new ArrayList<>();
                    for (String chapter : paper.getChapterScope()) {
                        String testJson = objectMapper.writeValueAsString(truncatedChapterScope);
                        if (testJson.length() + chapter.length() + 10 > MAX_JSON_LENGTH) {
                            break;
                        }
                        truncatedChapterScope.add(chapter);
                    }
                    paper.setChapterScope(truncatedChapterScope);
                }
            }
            
            // 处理questionConfig字段
            if (paper.getQuestionConfig() != null) {
                String questionConfigJson = objectMapper.writeValueAsString(paper.getQuestionConfig());
                if (questionConfigJson.length() > MAX_JSON_LENGTH) {
                    logger.warn("题目配置JSON数据过长，进行截断处理。原长度: {}, 限制长度: {}", 
                               questionConfigJson.length(), MAX_JSON_LENGTH);
                    // 截断处理：保留基本配置
                    Map<String, Integer> truncatedQuestionConfig = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : paper.getQuestionConfig().entrySet()) {
                        String testJson = objectMapper.writeValueAsString(truncatedQuestionConfig);
                        if (testJson.length() + entry.getKey().length() + 20 > MAX_JSON_LENGTH) {
                            break;
                        }
                        truncatedQuestionConfig.put(entry.getKey(), entry.getValue());
                    }
                    paper.setQuestionConfig(truncatedQuestionConfig);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON字段处理失败", e);
            // 设置默认值以避免保存失败
            if (paper.getChapterScope() == null) {
                paper.setChapterScope(new ArrayList<>());
            }
            if (paper.getQuestionConfig() == null) {
                paper.setQuestionConfig(new HashMap<>());
            }
        }
    }
    
    @Override
    public ExamPaperResponse createPaperFromAI(ExamPaperCreateRequest request, Long creatorId, String prompt) {
        validateCreateRequest(request, creatorId);
        
        // 创建试卷实体
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(request.getPaperName());
        paper.setCreatorId(creatorId);
        paper.setChapterScope(request.getChapterScope());
        paper.setQuestionConfig(request.getQuestionConfig());
        paper.setDifficulty(request.getDifficulty());
        paper.setTotalScore(request.getTotalScore());
        paper.setTotalQuestion(request.getTotalQuestion());
        paper.setExpireTime(request.getExpireTime());
        // generateTime在构造函数中自动设置
        paper.setStatus(ExamPaper.PaperStatus.published);
        
        // 验证和处理JSON字段
        validateAndProcessJsonFields(paper);
        
        // 保存试卷
        ExamPaper savedPaper = examPaperRepository.save(paper);
        
        // 基于大模型生成题目
        generateQuestionsFromAI(savedPaper, request, prompt);
        
        return convertToResponse(savedPaper);
    }
    
    @Override
    public ExamPaperResponse createPaperWithQuestions(ExamPaperCreateRequest request, Long creatorId) {
        validateCreateRequest(request, creatorId);
        
        // 调试输出：记录接收到的过期时间
        logger.info("=== 后端接收数据调试 ===");
        logger.info("接收到的过期时间 (request.getExpireTime()): {}", request.getExpireTime());
        if (request.getExpireTime() != null) {
            logger.info("过期时间对象: {}", request.getExpireTime());
            logger.info("过期时间字符串: {}", request.getExpireTime().toString());
        }
        logger.info("=========================");
        
        // 创建试卷实体
        ExamPaper paper = new ExamPaper();
        paper.setPaperName(request.getPaperName());
        paper.setCreatorId(creatorId);
        paper.setChapterScope(request.getChapterScope() != null ? request.getChapterScope() : new ArrayList<>());
        paper.setQuestionConfig(request.getQuestionConfig() != null ? request.getQuestionConfig() : new HashMap<>());
        paper.setDifficulty(request.getDifficulty());
        paper.setTotalScore(request.getTotalScore());
        paper.setTotalQuestion(request.getTotalQuestion());
        paper.setExpireTime(request.getExpireTime());
        paper.setStatus(ExamPaper.PaperStatus.published);
        
        // 调试输出：记录设置到实体的过期时间
        logger.info("=== 设置到实体的过期时间 ===");
        logger.info("实体中的过期时间 (paper.getExpireTime()): {}", paper.getExpireTime());
        logger.info("=========================");
        
        // 验证和处理JSON字段
        validateAndProcessJsonFields(paper);
        
        // 保存试卷
        ExamPaper savedPaper = examPaperRepository.save(paper);
        
        // 调试输出：记录保存到数据库后的过期时间
        logger.info("=== 保存到数据库后的过期时间 ===");
        logger.info("数据库中的过期时间 (savedPaper.getExpireTime()): {}", savedPaper.getExpireTime());
        logger.info("试卷ID: {}", savedPaper.getPaperId());
        logger.info("试卷名称: {}", savedPaper.getPaperName());
        logger.info("=========================");
        
        // 保存已生成的题目
        saveQuestionsFromRequest(savedPaper.getPaperId(), request.getQuestions());
        
        return convertToResponse(savedPaper);
    }
    
    @Override
    public ExamPaperResponse getPaper(Long paperId) {
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(paperId);
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        return convertToResponse(paperOpt.get());
    }
    
    @Override
    public PageResponse<ExamPaperResponse> getPapers(com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest) {
        pageRequest.validate();
        
        // 设置默认排序字段
        String sortBy = pageRequest.getSortBy() != null ? pageRequest.getSortBy() : "generateTime";
        Sort sort = Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().toUpperCase()), sortBy);
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize(), sort);
        
        Page<ExamPaper> paperPage = examPaperRepository.findAll(springPageRequest);
        
        List<ExamPaperResponse> responses = paperPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(responses, pageRequest.getPage(), 
                 pageRequest.getSize(), paperPage.getTotalElements());
    }
    
    @Override
    public ExamPaperResponse updatePaper(Long paperId, ExamPaperCreateRequest request) {
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(paperId);
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        
        ExamPaper paper = paperOpt.get();
        if (paper.getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷不能修改");
        }
        
        // 更新试卷信息
        if (request.getPaperName() != null) {
            paper.setPaperName(request.getPaperName());
        }
        if (request.getChapterScope() != null) {
            paper.setChapterScope(request.getChapterScope());
        }
        if (request.getQuestionConfig() != null) {
            paper.setQuestionConfig(request.getQuestionConfig());
        }
        if (request.getDifficulty() != null) {
            paper.setDifficulty(request.getDifficulty());
        }
        if (request.getTotalScore() != null) {
            paper.setTotalScore(request.getTotalScore());
        }
        if (request.getTotalQuestion() != null) {
            paper.setTotalQuestion(request.getTotalQuestion());
        }
        if (request.getExpireTime() != null) {
            paper.setExpireTime(request.getExpireTime());
        }
        
        // 验证和处理JSON字段
        validateAndProcessJsonFields(paper);
        
        ExamPaper savedPaper = examPaperRepository.save(paper);
        return convertToResponse(savedPaper);
    }
    
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deletePaper(Long paperId) {
        logger.info("开始删除试卷，ID: {}", paperId);
        
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(paperId);
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        
        ExamPaper paper = paperOpt.get();
        if (paper.getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷不能删除");
        }
        
        try {
            // 按正确顺序删除关联数据，避免外键约束违反
            
            // 1. 先删除所有相关的答题记录
            logger.info("删除试卷 {} 的所有答题记录", paperId);
            examAnswerRepository.deleteByPaperId(paperId);
            logger.info("✅ 成功删除答题记录");
            
            // 2. 再删除所有相关的考试结果
            logger.info("删除试卷 {} 的所有考试结果", paperId);
            examResultRepository.deleteByPaperId(paperId);
            logger.info("✅ 成功删除考试结果");
        
            // 3. 然后删除试卷题目
            logger.info("删除试卷 {} 的所有题目", paperId);
            examQuestionService.deleteQuestionsByPaper(paperId);
            logger.info("✅ 成功删除试卷题目");
            
            // 4. 最后删除试卷本身
            logger.info("删除试卷 {} 本身", paperId);
        examPaperRepository.deleteById(paperId);
            logger.info("✅ 成功删除试卷，ID: {}", paperId);
            
        } catch (Exception e) {
            logger.error("删除试卷时发生错误，ID: {}, 错误信息: {}", paperId, e.getMessage(), e);
            throw new CustomException("删除试卷失败：" + e.getMessage());
        }
    }
    
    @Override
    public ExamPaperResponse publishPaper(Long paperId) {
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(paperId);
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        
        ExamPaper paper = paperOpt.get();
        if (paper.getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("试卷已经发布");
        }
        
        // 检查试卷是否有题目
        long questionCount = examQuestionRepository.countByPaperId(paperId);
        if (questionCount == 0) {
            throw new CustomException("试卷没有题目，无法发布");
        }
        
        paper.setStatus(ExamPaper.PaperStatus.published);
        ExamPaper savedPaper = examPaperRepository.save(paper);
        
        return convertToResponse(savedPaper);
    }
    
    @Override
    @Transactional
    public ExamPaperResponse unpublishPaper(Long paperId) {
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(paperId);
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        
        ExamPaper paper = paperOpt.get();
        if (paper.getStatus() != ExamPaper.PaperStatus.published) {
            throw new CustomException("试卷未发布，无法取消发布");
        }
        
        paper.setStatus(ExamPaper.PaperStatus.draft);
        ExamPaper savedPaper = examPaperRepository.save(paper);
        
        return convertToResponse(savedPaper);
    }
    
    @Override
    public PageResponse<ExamPaperResponse> getUserPapers(Long creatorId, com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest) {
        pageRequest.validate();
        
        // 设置默认排序字段
        String sortBy = pageRequest.getSortBy() != null ? pageRequest.getSortBy() : "generateTime";
        Sort sort = Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().toUpperCase()), sortBy);
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize(), sort);
        
        Page<ExamPaper> paperPage = examPaperRepository.findByCreatorId(creatorId, springPageRequest);
        
        List<ExamPaperResponse> responses = paperPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(responses, pageRequest.getPage(), 
                 pageRequest.getSize(), paperPage.getTotalElements());
    }
    
    @Override
    public PageResponse<ExamPaperResponse> getAvailablePapers(com.dpsk.dpsk_quiz_sys_java.pojo.dto.PageRequest pageRequest) {
        pageRequest.validate();
        
        logger.info("=== getAvailablePapers 调试开始 ===");
        logger.info("请求参数 - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSortBy(), pageRequest.getSortDirection());
        
        // 设置默认排序字段
        String sortBy = pageRequest.getSortBy() != null ? pageRequest.getSortBy() : "generateTime";
        Sort sort = Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().toUpperCase()), sortBy);
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize(), sort);
        
        // 获取所有已发布的试卷
        Page<ExamPaper> allPublishedPapers = examPaperRepository.findByStatus(ExamPaper.PaperStatus.published, springPageRequest);
        logger.info("数据库查询结果 - 已发布试卷总数: {}, 当前页试卷数: {}", 
                   allPublishedPapers.getTotalElements(), allPublishedPapers.getContent().size());
        
        // 打印所有已发布试卷的详细信息
        for (ExamPaper paper : allPublishedPapers.getContent()) {
            logger.info("试卷详情 - ID: {}, 名称: {}, 状态: {}, 生成时间: {}, 过期时间: {}", 
                       paper.getPaperId(), paper.getPaperName(), paper.getStatus(), 
                       paper.getGenerateTime(), paper.getExpireTime());
        }
        
        LocalDateTime now = LocalDateTime.now();
        logger.info("当前时间: {}", now);
        
        // 过滤掉已过期的试卷（expireTime不为null且小于当前时间的试卷）
        List<ExamPaper> availablePapers = allPublishedPapers.getContent().stream()
                .filter(paper -> {
                    boolean isAvailable = paper.getExpireTime() == null || paper.getExpireTime().isAfter(now);
                    logger.info("试卷过滤检查 - ID: {}, 过期时间: {}, 是否可用: {}", 
                               paper.getPaperId(), paper.getExpireTime(), isAvailable);
                    return isAvailable;
                })
                .collect(Collectors.toList());
        
        logger.info("过滤后可用试卷数量: {}", availablePapers.size());
        
        List<ExamPaperResponse> responses = availablePapers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        logger.info("最终返回试卷数量: {}", responses.size());
        logger.info("=== getAvailablePapers 调试结束 ===");
        
        return PageResponse.of(responses, pageRequest.getPage(), pageRequest.getSize(), 
                (long) availablePapers.size());
    }
    
    @Override
    public ExamPaperResponse copyPaper(Long paperId, Long creatorId, String newPaperName) {
        Optional<ExamPaper> originalPaperOpt = examPaperRepository.findById(paperId);
        if (!originalPaperOpt.isPresent()) {
            throw new CustomException("原试卷不存在");
        }
        
        ExamPaper originalPaper = originalPaperOpt.get();
        
        // 创建新试卷
        ExamPaper newPaper = new ExamPaper();
        newPaper.setPaperName(newPaperName);
        newPaper.setCreatorId(creatorId);
        newPaper.setChapterScope(originalPaper.getChapterScope());
        newPaper.setQuestionConfig(originalPaper.getQuestionConfig());
        newPaper.setDifficulty(originalPaper.getDifficulty());
        newPaper.setTotalScore(originalPaper.getTotalScore());
        newPaper.setTotalQuestion(originalPaper.getTotalQuestion());
        newPaper.setExpireTime(originalPaper.getExpireTime());
        // generateTime在构造函数中自动设置
        newPaper.setStatus(ExamPaper.PaperStatus.draft);
        
        // 验证和处理JSON字段
        validateAndProcessJsonFields(newPaper);
        
        ExamPaper savedPaper = examPaperRepository.save(newPaper);
        
        // 复制题目
        List<ExamQuestion> originalQuestions = examQuestionRepository.findByPaperIdOrderBySort(paperId);
        for (ExamQuestion originalQuestion : originalQuestions) {
            ExamQuestion newQuestion = new ExamQuestion();
            newQuestion.setPaperId(savedPaper.getPaperId());
            newQuestion.setQuestionType(originalQuestion.getQuestionType());
            newQuestion.setQuestionContent(originalQuestion.getQuestionContent());
            newQuestion.setOptions(originalQuestion.getOptions());
            newQuestion.setCorrectAnswer(originalQuestion.getCorrectAnswer());
            newQuestion.setScore(originalQuestion.getScore());
            newQuestion.setAnalysis(originalQuestion.getAnalysis());
            newQuestion.setChunkId(originalQuestion.getChunkId());
            newQuestion.setSort(originalQuestion.getSort());
            
            examQuestionRepository.save(newQuestion);
        }
        
        return convertToResponse(savedPaper);
    }

    @Override
    public ExamPaper getById(Long paperId) {
        // 调用 JPA 提供的 findById 方法，返回 Optional 对象，不存在时返回 null
        return examPaperRepository.findById(paperId).orElse(null);
    }

    @Override
    public ExamPaper getById_Mapper(Long paperId) {
        return examPaperRepository.getById_Mapper(paperId);
    }

    @Override
    public Set<ExamQuestion.QuestionType> getQuestionTypes(Long paperId) {
        return examQuestionRepository.findByPaperId(paperId).stream()
                .map(ExamQuestion::getQuestionType)
                .collect(Collectors.toSet());
    }

    private void validateCreateRequest(ExamPaperCreateRequest request, Long creatorId) {
        if (request.getPaperName() == null || request.getPaperName().trim().isEmpty()) {
            throw new CustomException("试卷名称不能为空");
        }
        
        Optional<User> userOpt = userRepository.findById(creatorId);
        if (!userOpt.isPresent()) {
            throw new CustomException("用户不存在");
        }
    }
    
    private void generateQuestionsFromKnowledgeBase(ExamPaper paper, ExamPaperCreateRequest request) {
        // TODO: 实现基于知识库的题目生成逻辑
        // 1. 根据chapterScope查询相关的KbChunk
        // 2. 根据questionConfig生成不同类型的题目
        // 3. 调用AI服务基于知识块内容生成题目
        
        // 示例实现（需要根据实际需求完善）
        List<KbChunk> chunks = kbChunkRepository.findByChapterNameIn(request.getChapterScope());
        
        int sortOrder = 1;
        for (Map.Entry<String, Integer> config : request.getQuestionConfig().entrySet()) {
            String questionType = config.getKey();
            Integer count = config.getValue();
            
            for (int i = 0; i < count && i < chunks.size(); i++) {
                KbChunk chunk = chunks.get(i);
                
                // 创建题目请求
                ExamQuestionCreateRequest questionRequest = new ExamQuestionCreateRequest();
                questionRequest.setPaperId(paper.getPaperId());
                questionRequest.setQuestionType(ExamQuestion.QuestionType.valueOf(questionType));
                questionRequest.setChunkId(chunk.getChunkId());
                questionRequest.setSortOrder(sortOrder++);
                
                // 调用题目服务生成题目
                examQuestionService.createQuestionFromChunk(questionRequest, chunk.getContent());
            }
        }
    }
    
    private void generateQuestionsFromAI(ExamPaper paper, ExamPaperCreateRequest request, String prompt) {
        try {
            // 构建完整的prompt，包含用户输入的prompt和题目要求
            String fullPrompt = buildFullPrompt(prompt, request);
            
            // 调用AI服务生成题目
            List<Map<String, Object>> aiQuestions = aiService.generateQuestionsFromPrompt(
                fullPrompt, request.getTotalQuestion(), request.getDifficulty().toString());
            
            // 解析AI响应并创建题目
            parseAndCreateQuestions(paper.getPaperId(), aiQuestions);
            
        } catch (Exception e) {
            // 如果AI生成失败，创建默认题目作为回退
            createDefaultQuestions(paper, request);
        }
    }
    
    private String buildFullPrompt(String userPrompt, ExamPaperCreateRequest request) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请根据以下要求生成试卷题目：\n");
        promptBuilder.append("主题：").append(userPrompt).append("\n");
        promptBuilder.append("难度：").append(request.getDifficulty()).append("\n");
        promptBuilder.append("总题数：").append(request.getTotalQuestion()).append("\n");
        // ExamPaperCreateRequest中没有getDescription方法
        // if (request.getDescription() != null) {
        //     promptBuilder.append("试卷描述：").append(request.getDescription()).append("\n");
        // }
        promptBuilder.append("请生成JSON格式的题目，包含题目内容、选项、正确答案、解析等信息。");
        promptBuilder.append("题目类型包括单选题、多选题和判断题。");
        
        return promptBuilder.toString();
    }
    
    private void parseAndCreateQuestions(Long paperId, List<Map<String, Object>> aiQuestions) {
        int sortOrder = 1;
        for (Map<String, Object> questionData : aiQuestions) {
            ExamQuestion question = new ExamQuestion();
            question.setPaperId(paperId);
            question.setQuestionContent((String) questionData.get("question"));
            // 将String转换为List<String>
            String optionsStr = (String) questionData.get("options");
            question.setOptions(optionsStr != null ? Arrays.asList(optionsStr.split("\n")) : null);
            
            String correctAnswerStr = (String) questionData.get("correctAnswer");
            question.setCorrectAnswer(correctAnswerStr != null ? Arrays.asList(correctAnswerStr.split(",")) : null);
            question.setAnalysis((String) questionData.get("analysis"));
            question.setQuestionType(parseQuestionType((String) questionData.get("type")));
            question.setScore(parseScore(questionData.get("score")));
            question.setSort(sortOrder++);
            
            examQuestionRepository.save(question);
        }
    }
    
    private ExamQuestion.QuestionType parseQuestionType(String type) {
        if (type == null) return ExamQuestion.QuestionType.single;
        
        switch (type.toLowerCase()) {
            case "single":
            case "单选":
            case "单选题":
                return ExamQuestion.QuestionType.single;
            case "multi":
            case "多选":
            case "多选题":
                return ExamQuestion.QuestionType.multi;
            case "judge":
            case "判断":
            case "判断题":
                return ExamQuestion.QuestionType.judge;
            default:
                return ExamQuestion.QuestionType.single;
        }
    }
    
    private Integer parseScore(Object scoreObj) {
        if (scoreObj == null) return 5;
        
        if (scoreObj instanceof Integer) {
            return (Integer) scoreObj;
        } else if (scoreObj instanceof String) {
            try {
                return Integer.parseInt((String) scoreObj);
            } catch (NumberFormatException e) {
                return 5;
            }
        }
        return 5;
    }
    
    private void createDefaultQuestions(ExamPaper paper, ExamPaperCreateRequest request) {
        // 创建默认题目（当AI生成失败时使用）
        int questionCount = request.getTotalQuestion();
        
        for (int i = 1; i <= questionCount; i++) {
            ExamQuestion question = new ExamQuestion();
            question.setPaperId(paper.getPaperId());
            question.setQuestionContent("这是第" + i + "道关于" + paper.getPaperName() + "的题目");
            question.setOptions(Arrays.asList("A. 选项A", "B. 选项B", "C. 选项C", "D. 选项D"));
            question.setCorrectAnswer(Arrays.asList("A"));
            question.setAnalysis("这是第" + i + "道题目的解析");
            question.setQuestionType(i % 3 == 0 ? ExamQuestion.QuestionType.judge :
                                   i % 2 == 0 ? ExamQuestion.QuestionType.multi :
                                   ExamQuestion.QuestionType.single);
            question.setScore(question.getQuestionType() == ExamQuestion.QuestionType.judge ? 3 :
                            question.getQuestionType() == ExamQuestion.QuestionType.multi ? 8 : 5);
            question.setSort(i);
            // ExamQuestion实体类没有createTime和updateTime字段
            
            examQuestionRepository.save(question);
        }
    }

    private void saveQuestionsFromRequest(Long paperId, List<ExamPaperCreateRequest.QuestionItem> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        
        int sortOrder = 1;
        for (ExamPaperCreateRequest.QuestionItem questionItem : questions) {
            try {
                ExamQuestion question = new ExamQuestion();
                question.setPaperId(paperId);
                
                // 验证和处理题目内容长度
                String questionContent = questionItem.getQuestionText();
                if (questionContent != null && questionContent.length() > 1000) {
                    questionContent = questionContent.substring(0, 1000) + "...";
                    logger.warn("题目内容过长，已截断: {}", questionContent.substring(0, 50));
                }
                question.setQuestionContent(questionContent);
                
                // 验证和处理选项数据
                List<String> options = questionItem.getOptions();
                if (options != null && options.size() > 10) {
                    options = options.subList(0, 10); // 限制选项数量
                    logger.warn("选项数量过多，已截断为前10个");
                }
                question.setOptions(options);
                
                // 处理正确答案
                if (questionItem.getCorrectAnswer() != null) {
                    String correctAnswerStr = questionItem.getCorrectAnswer();
                    if (correctAnswerStr.length() > 500) {
                        correctAnswerStr = correctAnswerStr.substring(0, 500);
                        logger.warn("正确答案过长，已截断");
                    }
                    
                    // 如果是字符串，转换为列表
                    if (correctAnswerStr.contains(",")) {
                        question.setCorrectAnswer(Arrays.asList(correctAnswerStr.split(",")));
                    } else {
                        question.setCorrectAnswer(Arrays.asList(correctAnswerStr));
                    }
                }
                
                // 验证和处理解析内容
                String analysis = questionItem.getExplanation();
                if (analysis != null && analysis.length() > 1000) {
                    analysis = analysis.substring(0, 1000) + "...";
                    logger.warn("题目解析过长，已截断");
                }
                question.setAnalysis(analysis);
                
                // 处理题目类型
                String questionTypeStr = questionItem.getQuestionType();
                logger.debug("处理题目类型: {}", questionTypeStr);
                ExamQuestion.QuestionType questionType = parseQuestionTypeFromString(questionTypeStr);
                question.setQuestionType(questionType);
                
                question.setScore(questionItem.getScore() != null ? questionItem.getScore() : 5);
                question.setSort(sortOrder++);
                
                examQuestionRepository.save(question);
                logger.debug("成功保存题目: {}", question.getQuestionContent().substring(0, Math.min(50, question.getQuestionContent().length())));
                
            } catch (Exception e) {
                logger.error("保存题目失败: {}, 错误: {}", questionItem.getQuestionText(), e.getMessage());
                // 继续处理下一个题目，不中断整个流程
            }
        }
    }
    
    private ExamQuestion.QuestionType parseQuestionTypeFromString(String type) {
        if (type == null) return ExamQuestion.QuestionType.SINGLE_CHOICE;
        
        switch (type.toUpperCase()) {
            case "SINGLE_CHOICE":
            case "SINGLE":
            case "单选":
            case "单选题":
                return ExamQuestion.QuestionType.SINGLE_CHOICE;
            case "MULTI_CHOICE":
            case "MULTI":
            case "多选":
            case "多选题":
                return ExamQuestion.QuestionType.MULTI_CHOICE;
            case "JUDGE":
            case "判断":
            case "判断题":
                return ExamQuestion.QuestionType.JUDGE;
            case "SHORT_ANSWER":
            case "简答":
            case "简答题":
                return ExamQuestion.QuestionType.SHORT_ANSWER;
            case "FILL_BLANK":
            case "填空":
            case "填空题":
                return ExamQuestion.QuestionType.FILL_BLANK;
            default:
                return ExamQuestion.QuestionType.SINGLE_CHOICE;
        }
    }
    
    private ExamPaperResponse convertToResponse(ExamPaper paper) {
        ExamPaperResponse response = new ExamPaperResponse();
        response.setPaperId(paper.getPaperId());
        response.setPaperName(paper.getPaperName());
        response.setGenerateTime(paper.getGenerateTime());
        response.setCreatorId(paper.getCreatorId());
        // ExamPaperResponse中没有setDescription方法
        // response.setDescription(null);
        response.setDifficulty(paper.getDifficulty());
        response.setTotalScore(paper.getTotalScore());
        response.setTotalQuestion(paper.getTotalQuestion()); // 设置totalQuestion字段
        response.setQuestionCount(paper.getTotalQuestion()); // 同时设置questionCount字段以保持兼容性
        // ExamPaperResponse中没有setTimeLimit方法
        // response.setTimeLimit(null);
        response.setStatus(paper.getStatus());
        response.setExpireTime(paper.getExpireTime());
        
        // 获取创建者姓名
        Optional<User> userOpt = userRepository.findById(paper.getCreatorId());
        response.setCreatorName(userOpt.map(User::getUsername).orElse("未知用户"));
        
        // 获取实际题目数量
        long actualQuestionCount = examQuestionRepository.countByPaperId(paper.getPaperId());
        // ExamPaperResponse中没有setActualQuestionCount方法
        // response.setActualQuestionCount((int) actualQuestionCount);
        
        // TODO: 获取参与人数（从ExamResult表中统计）
        response.setParticipantCount(0);
        
        return response;
    }
}