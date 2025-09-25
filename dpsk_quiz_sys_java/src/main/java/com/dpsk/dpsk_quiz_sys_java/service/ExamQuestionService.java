package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import com.dpsk.dpsk_quiz_sys_java.repository.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IAIService;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ExamQuestionService implements IExamQuestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamQuestionService.class);
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private ExamPaperRepository examPaperRepository;
    
    @Autowired
    private KbChunkRepository kbChunkRepository;
    
    @Autowired
    private IAIService aiService;
    
    @Override
    public ExamQuestionResponse createQuestion(ExamQuestionCreateRequest request) {
        validateCreateRequest(request);
        
        ExamQuestion question = new ExamQuestion();
        question.setPaperId(request.getPaperId());
        question.setQuestionType(request.getQuestionType());
        question.setQuestionContent(request.getQuestionContent());
        
        // 处理选项数据类型转换，确保不为null
        List<String> optionsList = new ArrayList<>();
        if (request.getQuestionOptions() != null) {
            // 将Map<String, Object>转换为List<String>
            Object optionsObj = request.getQuestionOptions().get("options");
            if (optionsObj instanceof String) {
                String optionsStr = (String) optionsObj;
                if (!optionsStr.trim().isEmpty()) {
                    optionsList = Arrays.asList(optionsStr.split("\n"));
                }
            } else if (optionsObj instanceof java.util.List) {
                optionsList = (java.util.List<String>) optionsObj;
            }
        }
        question.setOptions(optionsList);
        
        // 处理正确答案数据类型转换，确保不为null
         List<String> correctAnswerList = new ArrayList<>();
         if (request.getCorrectAnswer() != null && !request.getCorrectAnswer().trim().isEmpty()) {
             correctAnswerList = Arrays.asList(request.getCorrectAnswer().split(","));
        }
         question.setCorrectAnswer(correctAnswerList);
        
        question.setScore(request.getScore());
        
        // 设置默认解析（ExamQuestionCreateRequest中没有analysis字段）
        question.setAnalysis("题目解析");
        question.setChunkId(request.getChunkId());
        
        // 设置排序值
        if (request.getSortOrder() != null) {
            question.setSort(request.getSortOrder());
        } else {
            // 自动设置为最后一个
            Integer maxSort = examQuestionRepository.findMaxSortByPaperId(request.getPaperId());
            question.setSort(maxSort != null ? maxSort + 1 : 1);
        }
        
        ExamQuestion savedQuestion = examQuestionRepository.save(question);
        return convertToResponse(savedQuestion, true);
    }
    
    @Override
    public ExamQuestionResponse createQuestionFromChunk(ExamQuestionCreateRequest request, String chunkContent) {
        logger.info("开始创建题目 - paperId: {}, questionType: {}, chunkId: {}", 
            request.getPaperId(), request.getQuestionType(), request.getChunkId());
        
        validateCreateRequest(request);
        
        ExamQuestion question = new ExamQuestion();
        question.setPaperId(request.getPaperId());
        question.setQuestionType(request.getQuestionType());
        question.setChunkId(request.getChunkId());
        
        // 设置排序值
        if (request.getSortOrder() != null) {
            question.setSort(request.getSortOrder());
        } else {
            Integer maxSort = examQuestionRepository.findMaxSortByPaperId(request.getPaperId());
            question.setSort(maxSort != null ? maxSort + 1 : 1);
        }
        
        logger.info("题目基本信息设置完成，开始生成题目内容 - paperId: {}, sort: {}", 
            request.getPaperId(), question.getSort());
        
        // 根据题目类型和知识块内容生成题目
        generateQuestionContent(question, chunkContent);
        
        logger.info("题目内容生成完成，准备保存到数据库 - paperId: {}, questionContent: {}", 
            request.getPaperId(), question.getQuestionContent() != null ? question.getQuestionContent().substring(0, Math.min(50, question.getQuestionContent().length())) : "null");
        
        try {
            ExamQuestion savedQuestion = examQuestionRepository.save(question);
            logger.info("题目保存成功 - questionId: {}, paperId: {}", 
                savedQuestion.getQuestionId(), savedQuestion.getPaperId());
            return convertToResponse(savedQuestion, true);
        } catch (Exception e) {
            logger.error("题目保存失败 - paperId: {}, error: {}", request.getPaperId(), e.getMessage(), e);
            throw new CustomException("题目保存失败: " + e.getMessage());
        }
    }
    
    @Override
    public ExamQuestionResponse getQuestion(Long questionId) {
        Optional<ExamQuestion> questionOpt = examQuestionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            throw new CustomException("题目不存在");
        }
        return convertToResponse(questionOpt.get(), true);
    }
    public Set<QuestionType> getQuestionTypes(Long paperId) {
        return examQuestionRepository.findByPaperId(paperId).stream()
                .map(ExamQuestion::getQuestionType)
                .collect(Collectors.toSet());
    }
    
    @Override
    public List<ExamQuestionResponse> getQuestionsByPaper(Long paperId, boolean showAnswer) {
        List<ExamQuestion> questions = examQuestionRepository.findByPaperIdOrderBySort(paperId);
        return questions.stream()
                .map(question -> convertToResponse(question, showAnswer))
                .collect(Collectors.toList());
    }
    
    @Override
    public ExamQuestionResponse updateQuestion(Long questionId, ExamQuestionCreateRequest request) {
        Optional<ExamQuestion> questionOpt = examQuestionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            throw new CustomException("题目不存在");
        }
        
        ExamQuestion question = questionOpt.get();
        
        // 检查试卷状态
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(question.getPaperId());
        if (paperOpt.isPresent() && paperOpt.get().getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷题目不能修改");
        }
        
        // 更新题目信息
        if (request.getQuestionType() != null) {
            question.setQuestionType(request.getQuestionType());
        }
        if (request.getQuestionContent() != null) {
            question.setQuestionContent(request.getQuestionContent());
        }
        if (request.getQuestionOptions() != null) {
            // 将Map<String, Object>转换为List<String>
            Object optionsObj = request.getQuestionOptions().get("options");
            if (optionsObj instanceof String) {
                question.setOptions(Arrays.asList(((String) optionsObj).split("\n")));
            } else if (optionsObj instanceof java.util.List) {
                question.setOptions((java.util.List<String>) optionsObj);
            }
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(Arrays.asList(request.getCorrectAnswer().split(",")));
        }
        if (request.getScore() != null) {
            question.setScore(request.getScore());
        }
        // ExamQuestionCreateRequest中没有analysis字段，保持原有解析不变
        if (request.getChunkId() != null) {
            question.setChunkId(request.getChunkId());
        }
        
        ExamQuestion savedQuestion = examQuestionRepository.save(question);
        return convertToResponse(savedQuestion, true);
    }
    
    @Override
    public void deleteQuestion(Long questionId) {
        Optional<ExamQuestion> questionOpt = examQuestionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            throw new CustomException("题目不存在");
        }
        
        ExamQuestion question = questionOpt.get();
        
        // 检查试卷状态
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(question.getPaperId());
        if (paperOpt.isPresent() && paperOpt.get().getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷题目不能删除");
        }
        
        examQuestionRepository.deleteById(questionId);
    }
    
    @Override
    @Transactional
    public void deleteQuestionsByPaper(Long paperId) {
        examQuestionRepository.deleteByPaperId(paperId);
    }
    
    @Override
    public long getQuestionCount(Long paperId) {
        return examQuestionRepository.countByPaperId(paperId);
    }
    
    @Override
    public void updateQuestionSort(Long questionId, Integer newSort) {
        Optional<ExamQuestion> questionOpt = examQuestionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            throw new CustomException("题目不存在");
        }
        
        ExamQuestion question = questionOpt.get();
        
        // 检查试卷状态
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(question.getPaperId());
        if (paperOpt.isPresent() && paperOpt.get().getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷题目顺序不能修改");
        }
        
        question.setSort(newSort);
        examQuestionRepository.save(question);
    }
    @Transactional(readOnly = true)
    @Override
    public List<ExamQuestion> getByPaperId(Long paperId) {

//        return examQuestionRepository.findByPaperId(paperId);
        return examQuestionRepository.findByPaperIdWithFetch(paperId);
    }

    private void validateCreateRequest(ExamQuestionCreateRequest request) {
        if (request.getPaperId() == null) {
            throw new CustomException("试卷ID不能为空");
        }
        
        if (request.getQuestionType() == null) {
            throw new CustomException("题目类型不能为空");
        }
        
        // 检查试卷是否存在
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(request.getPaperId());
        if (!paperOpt.isPresent()) {
            throw new CustomException("试卷不存在");
        }
        
        // 检查试卷状态
        if (paperOpt.get().getStatus() == ExamPaper.PaperStatus.published) {
            throw new CustomException("已发布的试卷不能添加题目");
        }
        
        // 检查知识块是否存在（如果指定了chunkId）
        if (request.getChunkId() != null) {
            Optional<KbChunk> chunkOpt = kbChunkRepository.findById(request.getChunkId());
            if (!chunkOpt.isPresent()) {
                throw new CustomException("知识块不存在");
            }
        }
    }
    
    private void generateQuestionContent(ExamQuestion question, String chunkContent) {
        logger.info("开始生成题目内容 - questionType: {}, chunkContentLength: {}", 
            question.getQuestionType(), chunkContent != null ? chunkContent.length() : 0);
        
        try {
            // 调用AI服务生成题目内容
            logger.info("调用AI服务生成题目 - questionType: {}", question.getQuestionType());
            Map<String, Object> questionData = aiService.generateQuestionFromChunk(
                chunkContent, question.getQuestionType(), "medium");
            
            logger.info("AI服务调用成功，开始设置题目内容 - questionData keys: {}", 
                questionData != null ? questionData.keySet() : "null");
            
            // 设置题目内容
            question.setQuestionContent((String) questionData.get("question"));
            
            // 将String转换为List<String>
            String optionsStr = (String) questionData.get("options");
            question.setOptions(optionsStr != null ? Arrays.asList(optionsStr.split("\n")) : null);
            
            String correctAnswerStr = (String) questionData.get("correctAnswer");
            question.setCorrectAnswer(correctAnswerStr != null ? Arrays.asList(correctAnswerStr.split(",")) : null);
            
            question.setAnalysis((String) questionData.get("analysis"));
            
            // 根据题目类型设置默认分数
            logger.debug("根据题目类型设置默认分数 - questionType: {}", question.getQuestionType());
            switch (question.getQuestionType()) {
                case SINGLE_CHOICE:
                    question.setScore(5);
                    logger.debug("设置单选题分数: 5");
                    break;
                case MULTI_CHOICE:
                    question.setScore(8);
                    logger.debug("设置多选题分数: 8");
                    break;
                case JUDGE:
                    question.setScore(3);
                    logger.debug("设置判断题分数: 3");
                    break;
                default:
                    logger.warn("未知题目类型: {}, 使用默认分数5", question.getQuestionType());
                    question.setScore(5);
                    break;
            }
            logger.info("题目内容生成完成 - questionType: {}, score: {}", question.getQuestionType(), question.getScore());
        } catch (Exception e) {
            logger.error("AI生成题目失败，使用默认题目 - questionType: {}", question.getQuestionType(), e);
            generateFallbackQuestion(question, chunkContent);
        }
    }
    
    private void generateFallbackQuestion(ExamQuestion question, String chunkContent) {
        logger.info("生成默认题目（AI服务失败时使用） - questionType: {}", question.getQuestionType());
        
        // 生成默认题目（当AI服务失败时使用）
        switch (question.getQuestionType()) {
            case SINGLE_CHOICE:
                logger.debug("生成默认单选题");
                question.setQuestionContent("根据以下内容，请选择正确答案：" + chunkContent.substring(0, Math.min(100, chunkContent.length())));
                question.setOptions(Arrays.asList("A. 选项A", "B. 选项B", "C. 选项C", "D. 选项D"));
                question.setCorrectAnswer(Arrays.asList("A"));
                question.setScore(5);
                question.setAnalysis("这是一道基于知识库内容生成的单选题。");
                logger.debug("默认单选题生成完成 - score: 5");
                break;
            case MULTI_CHOICE:
                logger.debug("生成默认多选题");
                question.setQuestionContent("根据以下内容，请选择所有正确答案：" + chunkContent.substring(0, Math.min(100, chunkContent.length())));
                question.setOptions(Arrays.asList("A. 选项A", "B. 选项B", "C. 选项C", "D. 选项D"));
                question.setCorrectAnswer(Arrays.asList("A", "C"));
                question.setScore(8);
                question.setAnalysis("这是一道基于知识库内容生成的多选题。");
                logger.debug("默认多选题生成完成 - score: 8");
                break;
            case JUDGE:
                logger.debug("生成默认判断题");
                question.setQuestionContent("请判断以下说法是否正确：" + chunkContent.substring(0, Math.min(100, chunkContent.length())));
                question.setOptions(Arrays.asList("A. 正确", "B. 错误"));
                question.setCorrectAnswer(Arrays.asList("A"));
                question.setScore(3);
                question.setAnalysis("这是一道基于知识库内容生成的判断题。");
                logger.debug("默认判断题生成完成 - score: 3");
                break;
            default:
                logger.warn("未知题目类型，生成默认单选题 - questionType: {}", question.getQuestionType());
                question.setQuestionContent("根据以下内容，请选择正确答案：" + chunkContent.substring(0, Math.min(100, chunkContent.length())));
                question.setOptions(Arrays.asList("A. 选项A", "B. 选项B", "C. 选项C", "D. 选项D"));
                question.setCorrectAnswer(Arrays.asList("A"));
                question.setScore(5);
                question.setAnalysis("这是一道基于知识库内容生成的默认题目。");
                break;
        }
        logger.info("默认题目生成完成 - questionType: {}, score: {}", question.getQuestionType(), question.getScore());
    }
    
    private ExamQuestionResponse convertToResponse(ExamQuestion question, boolean showAnswer) {
        ExamQuestionResponse response = new ExamQuestionResponse();
        response.setQuestionId(question.getQuestionId());
        response.setPaperId(question.getPaperId());
        response.setQuestionType(question.getQuestionType());
        response.setQuestionContent(question.getQuestionContent());
        
        // 将List<String> options转换为Map<String, Object> questionOptions
        // 确保questionOptions字段始终存在，即使选项为空
        Map<String, Object> optionsMap = new HashMap<>();
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            optionsMap.put("options", question.getOptions());
        } else {
            optionsMap.put("options", new ArrayList<>());
        }
        response.setQuestionOptions(optionsMap);
        
        response.setScore(question.getScore());
        // ExamQuestionResponse中没有setSort方法
        // response.setSort(question.getSort());
        response.setChunkId(question.getChunkId());
        response.setShowAnswer(showAnswer);
        
        // 根据showAnswer决定是否显示答案和解析
        if (showAnswer) {
            // 安全处理correctAnswer字段，添加null检查和日志
            List<String> correctAnswerList = question.getCorrectAnswer();
            logger.info("处理correctAnswer字段 - questionId: {}, correctAnswerList: {}, isNull: {}", 
                question.getQuestionId(), correctAnswerList, correctAnswerList == null);
            
            if (correctAnswerList != null && !correctAnswerList.isEmpty()) {
                String correctAnswerStr = String.join(",", correctAnswerList);
                response.setCorrectAnswer(correctAnswerStr);
                logger.info("设置correctAnswer成功 - questionId: {}, correctAnswer: '{}'", 
                    question.getQuestionId(), correctAnswerStr);
            } else {
                // 如果correctAnswer为null或空，设置空字符串作为默认值
                response.setCorrectAnswer("");
                logger.warn("correctAnswer为空，设置默认空字符串 - questionId: {}", question.getQuestionId());
            }
            
            // 处理analysis字段
            String analysis = question.getAnalysis();
            response.setAnalysis(analysis != null ? analysis : "");
            logger.info("设置analysis - questionId: {}, analysis: '{}'", 
                question.getQuestionId(), analysis != null ? analysis : "null");
        }
        
        // 获取试卷名称
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(question.getPaperId());
        response.setPaperName(paperOpt.map(ExamPaper::getPaperName).orElse("未知试卷"));
        
        // 获取章节名称（如果有关联的知识块）
        if (question.getChunkId() != null) {
            Optional<KbChunk> chunkOpt = kbChunkRepository.findById(question.getChunkId());
            response.setChapterName(chunkOpt.map(KbChunk::getChapterName).orElse("未知章节"));
        }
        
        return response;
    }
}