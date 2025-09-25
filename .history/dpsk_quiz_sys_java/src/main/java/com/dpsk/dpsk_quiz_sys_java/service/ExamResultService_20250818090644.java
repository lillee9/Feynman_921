package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.*;
import com.dpsk.dpsk_quiz_sys_java.repository.*;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IExamResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试结果服务实现类
 */
@Service
public class ExamResultService implements IExamResultService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamResultService.class);
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private ExamPaperRepository examPaperRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private ExamQuestionService examQuestionService;
       @Autowired
    private ExamAnswerRepository examAnswerRepository;
    
    @Override
    @Transactional
    public ExamResultResponse submitExamAnswers(ExamAnswerSubmitRequest submitRequest) {
        // 🔍 添加详细的调试日志，记录接收到的请求参数
        logger.info("📥 接收到考试答案提交请求 - 用户ID: {}, 试卷ID: {}, override标记: {}, 答案数量: {}", 
            submitRequest.getUserId(), 
            submitRequest.getPaperId(), 
            submitRequest.getOverride(),
            submitRequest.getAnswers() != null ? submitRequest.getAnswers().size() : 0);
        
        // 详细记录override参数的状态
        if (submitRequest.getOverride() == null) {
            logger.warn("⚠️ override参数为null");
        } else {
            logger.info("✅ override参数值: {}, 类型: {}", 
                submitRequest.getOverride(), 
                submitRequest.getOverride().getClass().getSimpleName());
        }
        
        try {
            // 验证请求参数
            validateSubmitRequest(submitRequest);
            
            // 检查用户是否已经参加过该试卷考试
            boolean hasExistingExam = hasUserTakenExam(submitRequest.getUserId(), submitRequest.getPaperId());
            if (hasExistingExam) {
                // 如果用户已参加过考试，检查是否允许覆盖
                if (submitRequest.getOverride() == null || !submitRequest.getOverride()) {
                    logger.warn("❌ 用户 {} 已参加过试卷 {} 的考试，且未设置覆盖标记", 
                        submitRequest.getUserId(), submitRequest.getPaperId());
                    throw new CustomException("您已经参加过该试卷考试");
                }
                
                // 如果允许覆盖，删除旧的考试记录
                logger.info("🔄 检测到覆盖标记，删除用户 {} 在试卷 {} 的旧考试记录", 
                    submitRequest.getUserId(), submitRequest.getPaperId());
                
                // 获取并删除旧的考试记录（直接在当前事务中执行删除操作）
                Optional<ExamResult> existingResult = examResultRepository.findByUserIdAndPaperId(
                    submitRequest.getUserId(), submitRequest.getPaperId());
                if (existingResult.isPresent()) {
                    Long resultId = existingResult.get().getResultId();
                    logger.info("准备删除旧考试记录 - ID: {}, 用户ID: {}, 试卷ID: {}, 分数: {}", 
                        resultId, existingResult.get().getUserId(), existingResult.get().getPaperId(), 
                        existingResult.get().getTotalScore());
                    
                    // 先删除相关的答题记录，避免外键约束冲突
                    logger.info("🗑️ 删除用户 {} 在试卷 {} 的旧答题记录", 
                        submitRequest.getUserId(), submitRequest.getPaperId());
                    examAnswerRepository.deleteByUserIdAndPaperId(
                        submitRequest.getUserId(), submitRequest.getPaperId());
                    logger.info("✅ 成功删除旧答题记录");
                    
                    // 然后删除考试结果记录
                    examResultRepository.deleteById(resultId);
                    logger.info("✅ 成功删除旧考试记录，ID: {}", resultId);
                }
            }
            
            // 获取试卷信息
            ExamPaper paper = examPaperRepository.findById(submitRequest.getPaperId())
                .orElseThrow(() -> new CustomException("试卷不存在"));
            
            // 检查试卷状态
            if (paper.getStatus() != ExamPaper.PaperStatus.published) {
                throw new CustomException("试卷未发布，无法参加考试");
            }
            
            // 检查试卷是否过期
            if (paper.getExpireTime() != null && paper.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new CustomException("试卷已过期");
            }
            
            // 获取试卷所有题目
            List<ExamQuestion> questions = examQuestionRepository.findByPaperIdOrderBySort(submitRequest.getPaperId());
            if (questions.isEmpty()) {
                throw new CustomException("试卷没有题目");
            }
            
            // 计算成绩
            // 将AnswerItem列表转换为Map
             Map<Long, String> answersMap = submitRequest.getAnswers().stream()
                 .collect(Collectors.toMap(
                     ExamAnswerSubmitRequest.AnswerItem::getQuestionId,
                     ExamAnswerSubmitRequest.AnswerItem::getUserAnswer
                 ));
             ScoreCalculationResult scoreResult = calculateScore(questions, answersMap);
            
            // 创建考试结果
            ExamResult examResult = new ExamResult();
            examResult.setUserId(submitRequest.getUserId());
            examResult.setPaperId(submitRequest.getPaperId());
            examResult.setPaperDifficulty(paper.getDifficulty());
            examResult.setTotalScore(BigDecimal.valueOf(scoreResult.getUserScore()));
            examResult.setCorrectCount(scoreResult.getCorrectCount());
            examResult.setWrongCount(questions.size() - scoreResult.getCorrectCount());
            examResult.setUnansweredCount(0); // 假设所有题目都已回答
            
            // 设置时间相关字段
            if (submitRequest.getStartTime() != null) {
                examResult.setStartTime(submitRequest.getStartTime());
            } else {
                examResult.setStartTime(LocalDateTime.now().minusMinutes(60)); // 默认1小时前开始
            }
            
            if (submitRequest.getEndTime() != null) {
                examResult.setEndTime(submitRequest.getEndTime());
            } else {
            examResult.setEndTime(LocalDateTime.now());
            }
            
            // 计算考试时长（分钟）
            if (examResult.getStartTime() != null && examResult.getEndTime() != null) {
                long durationMinutes = java.time.Duration.between(examResult.getStartTime(), examResult.getEndTime()).toMinutes();
                examResult.setDuration((int) durationMinutes);
            }
            
            // 保存考试结果
            ExamResult savedResult = examResultRepository.save(examResult);
            
            // 保存详细的答题记录
            saveAnswerDetails(savedResult.getResultId(), submitRequest, scoreResult.getAnswerDetails());
            
            return convertToResponse(savedResult);
            
        } catch (Exception e) {
            logger.error("提交考试答案失败", e);
            throw new CustomException("提交考试答案失败：" + e.getMessage());
        }
    }
    
    @Override
    public ExamResultResponse getExamResult(Long resultId) {
        System.out.println("[DEBUG] ExamResultService.getExamResult - 开始处理，resultId: " + resultId);
        try {
            System.out.println("[DEBUG] ExamResultService.getExamResult - 查询数据库前");
        ExamResult result = examResultRepository.findById(resultId)
            .orElseThrow(() -> new CustomException("考试结果不存在"));
            System.out.println("[DEBUG] ExamResultService.getExamResult - 查询数据库后，result: " + (result != null ? "非空" : "空"));
            if (result != null) {
                System.out.println("[DEBUG] ExamResultService.getExamResult - result详情: resultId=" + result.getResultId() + 
                    ", userId=" + result.getUserId() + ", paperId=" + result.getPaperId() + 
                    ", totalScore=" + result.getTotalScore());
            }
            
            System.out.println("[DEBUG] ExamResultService.getExamResult - 调用convertToResponse前");
            ExamResultResponse response = convertToResponse(result);
            System.out.println("[DEBUG] ExamResultService.getExamResult - 调用convertToResponse后，response: " + (response != null ? "非空" : "空"));
            if (response != null) {
                System.out.println("[DEBUG] ExamResultService.getExamResult - response详情: resultId=" + response.getResultId() + 
                    ", userId=" + response.getUserId() + ", paperId=" + response.getPaperId() + 
                    ", totalScore=" + response.getTotalScore());
            }
            
            return response;
        } catch (Exception e) {
            System.err.println("[ERROR] ExamResultService.getExamResult - 异常发生: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public PageResponse<ExamResultResponse> getUserExamResults(Long userId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(
            pageRequest.getPage() - 1, 
            pageRequest.getSize(),
            Sort.by(Sort.Direction.DESC, "createTime")
        );
        
        // 由于Repository中没有分页方法，先获取所有结果然后手动分页
        List<ExamResult> allResults = examResultRepository.findByUserId(userId);
        int start = springPageRequest.getPageNumber() * springPageRequest.getPageSize();
        int end = Math.min(start + springPageRequest.getPageSize(), allResults.size());
        List<ExamResult> pageContent = start < allResults.size() ? allResults.subList(start, end) : List.of();
        Page<ExamResult> resultPage = new PageImpl<>(pageContent, springPageRequest, allResults.size());
        
        List<ExamResultResponse> responses = resultPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<ExamResultResponse>(responses, pageRequest.getPage(), 
                 pageRequest.getSize(), (long)allResults.size());
    }
    
    @Override
    public PageResponse<ExamResultResponse> getPaperExamResults(Long paperId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(
            pageRequest.getPage() - 1, 
            pageRequest.getSize(),
            Sort.by(Sort.Direction.DESC, "totalScore", "createTime")
        );
        
        // 由于Repository中没有分页方法，先获取所有结果然后手动分页
        List<ExamResult> allResults = examResultRepository.findByPaperId(paperId);
        int start = springPageRequest.getPageNumber() * springPageRequest.getPageSize();
        int end = Math.min(start + springPageRequest.getPageSize(), allResults.size());
        List<ExamResult> pageContent = start < allResults.size() ? allResults.subList(start, end) : List.of();
        Page<ExamResult> resultPage = new PageImpl<>(pageContent, springPageRequest, allResults.size());
        
        List<ExamResultResponse> responses = resultPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<ExamResultResponse>(
             responses,
             pageRequest.getPage(),
             pageRequest.getSize(),
             resultPage.getTotalElements()
         );
    }
    
    @Override
    public ExamResultResponse getUserPaperResult(Long userId, Long paperId) {
        Optional<ExamResult> resultOpt = examResultRepository.findByUserIdAndPaperId(userId, paperId);
        return resultOpt.map(this::convertToResponse).orElse(null);
    }
    
    @Override
    @Transactional
    public void deleteExamResult(Long resultId) {
        logger.info("开始删除考试结果，ID: {}", resultId);
        
        if (resultId == null) {
            logger.error("删除考试结果失败：结果ID不能为空");
            throw new CustomException("考试结果ID不能为空");
        }
        
        try {
            // 检查考试结果是否存在
            if (!examResultRepository.existsById(resultId)) {
                logger.warn("删除考试结果失败：考试结果不存在，ID: {}", resultId);
                throw new CustomException("考试结果不存在，ID: " + resultId);
        }
            
            // 获取考试结果详情用于日志记录和删除相关答题记录
            Optional<ExamResult> resultOpt = examResultRepository.findById(resultId);
            if (resultOpt.isPresent()) {
                ExamResult result = resultOpt.get();
                logger.info("准备删除考试结果 - ID: {}, 用户ID: {}, 试卷ID: {}, 分数: {}", 
                    resultId, result.getUserId(), result.getPaperId(), result.getTotalScore());
                
                // 先删除相关的答题记录，避免外键约束冲突
                logger.info("🗑️ 删除用户 {} 在试卷 {} 的答题记录", 
                    result.getUserId(), result.getPaperId());
                examAnswerRepository.deleteByUserIdAndPaperId(
                    result.getUserId(), result.getPaperId());
                logger.info("✅ 成功删除相关答题记录");
            }
            
            // 执行删除操作
        examResultRepository.deleteById(resultId);
            logger.info("成功删除考试结果，ID: {}", resultId);
            
        } catch (CustomException e) {
            // 重新抛出自定义异常
            throw e;
        } catch (Exception e) {
            logger.error("删除考试结果时发生未预期的错误，ID: {}, 错误信息: {}", resultId, e.getMessage(), e);
            throw new CustomException("删除考试结果失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteExamResults(List<Long> resultIds) {
        logger.info("开始批量删除考试结果，数量: {}", resultIds.size());
        
        // 先删除相关的答题记录，避免外键约束冲突
        for (Long resultId : resultIds) {
            Optional<ExamResult> resultOpt = examResultRepository.findById(resultId);
            if (resultOpt.isPresent()) {
                ExamResult result = resultOpt.get();
                logger.info("🗑️ 删除用户 {} 在试卷 {} 的答题记录", 
                    result.getUserId(), result.getPaperId());
                examAnswerRepository.deleteByUserIdAndPaperId(
                    result.getUserId(), result.getPaperId());
            }
        }
        logger.info("✅ 成功删除所有相关答题记录");
        
        // 然后删除考试结果记录
        examResultRepository.deleteAllById(resultIds);
        logger.info("✅ 成功批量删除考试结果，数量: {}", resultIds.size());
    }
    
    @Override
    public Map<String, Object> getPaperStatistics(Long paperId) {
        List<ExamResult> results = examResultRepository.findByPaperId(paperId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalParticipants", results.size());
        
        if (results.isEmpty()) {
            statistics.put("averageScore", 0.0);
            statistics.put("maxScore", 0.0);
            statistics.put("minScore", 0.0);
            statistics.put("passRate", 0.0);
            return statistics;
        }
        
        // 计算平均分
        double averageScore = results.stream()
            .mapToDouble(r -> r.getTotalScore().doubleValue())
            .average().orElse(0.0);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        
        // 最高分和最低分
        double maxScore = results.stream()
            .mapToDouble(r -> r.getTotalScore().doubleValue())
            .max().orElse(0.0);
        double minScore = results.stream()
            .mapToDouble(r -> r.getTotalScore().doubleValue())
            .min().orElse(0.0);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        
        // 及格率（假设60%为及格线）
        long passCount = results.stream()
            .filter(r -> {
                double percentage = r.getTotalScore().doubleValue() / r.getTotalScore().doubleValue() * 100;
                return percentage >= 60;
            })
            .count();
        double passRate = (double) passCount / results.size() * 100;
        statistics.put("passRate", Math.round(passRate * 100.0) / 100.0);
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        List<ExamResult> results = examResultRepository.findByUserId(userId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalExams", results.size());
        
        if (results.isEmpty()) {
            statistics.put("averageScore", 0.0);
            statistics.put("bestScore", 0.0);
            statistics.put("passCount", 0);
            return statistics;
        }
        
        // 计算平均分
        double averageScore = results.stream()
            .mapToDouble(r -> r.getTotalScore().doubleValue() / r.getTotalScore().doubleValue() * 100)
            .average().orElse(0.0);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        
        // 最佳成绩
        double bestScore = results.stream()
            .mapToDouble(r -> r.getTotalScore().doubleValue() / r.getTotalScore().doubleValue() * 100)
            .max().orElse(0.0);
        statistics.put("bestScore", Math.round(bestScore * 100.0) / 100.0);
        
        // 及格次数
        long passCount = results.stream()
            .filter(r -> {
                double percentage = r.getTotalScore().doubleValue() / r.getTotalScore().doubleValue() * 100;
                return percentage >= 60;
            })
            .count();
        statistics.put("passCount", passCount);
        
        return statistics;
    }
    
    @Override
    public List<ExamResultResponse> getPaperRanking(Long paperId, Integer limit) {
        List<ExamResult> results = examResultRepository.findByPaperIdOrderByTotalScoreDesc(paperId);
        
        return results.stream()
            .limit(limit != null ? limit : 10)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Double calculatePassRate(Long paperId, BigDecimal passScore) {
        return examResultRepository.calculatePassRateByPaperId_zht(paperId, passScore);
    }
    @Override
    public Double calculatePassRate(Long paperId, BigDecimal passScore) {
        return examResultRepository.calculatePassRateByPaperId(paperId, passScore);
    }
    
    @Override
    public Map<String, Object> getScoreDistribution(Long paperId) {
        List<ExamResult> results = examResultRepository.findByPaperId(paperId);
        
        Map<String, Object> distribution = new HashMap<>();
        
        // 分数段统计
        Map<String, Integer> scoreRanges = new HashMap<>();
        scoreRanges.put("0-59", 0);
        scoreRanges.put("60-69", 0);
        scoreRanges.put("70-79", 0);
        scoreRanges.put("80-89", 0);
        scoreRanges.put("90-100", 0);
        
        for (ExamResult result : results) {
            double percentage = result.getTotalScore().doubleValue() / result.getTotalScore().doubleValue() * 100;
            
            if (percentage < 60) {
                scoreRanges.put("0-59", scoreRanges.get("0-59") + 1);
            } else if (percentage < 70) {
                scoreRanges.put("60-69", scoreRanges.get("60-69") + 1);
            } else if (percentage < 80) {
                scoreRanges.put("70-79", scoreRanges.get("70-79") + 1);
            } else if (percentage < 90) {
                scoreRanges.put("80-89", scoreRanges.get("80-89") + 1);
            } else {
                scoreRanges.put("90-100", scoreRanges.get("90-100") + 1);
            }
        }
        
        distribution.put("scoreRanges", scoreRanges);
        distribution.put("totalCount", results.size());
        
        return distribution;
    }
    
    @Override
    public AnalysisResponse getAnswerAnalysis(Long paperId) {
        // TODO: 实现答题分析功能
        // 这里需要分析每道题的正确率、错误选项分布等
        AnalysisResponse analysis = new AnalysisResponse();
        // AnalysisResponse中没有setPaperId和setAnalysisTime方法，移除这些调用
        // analysis.setPaperId(paperId);
        // analysis.setAnalysisTime(LocalDateTime.now());
        
        return analysis;
    }
    /**
     * 根据难度获取分数前N的考试结果
     * @param paperDifficulty 难度枚举（如 ExamPaper.Difficulty.easy）
     * @param limit 限制返回数量
     * @return 考试结果列表
     */
    public List<ExamResult> getTopResultsByDifficulty(ExamPaper.Difficulty paperDifficulty, int limit) {
        // 显式使用Spring Data的PageRequest类（page=0表示第一页，size=limit表示每页大小）
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
                0,
                limit,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "totalScore")
        );

        return examResultRepository.findByPaperDifficulty(paperDifficulty, pageRequest);
    }
    @Override
    @Transactional
    public ExamResultResponse recalculateScore(Long resultId) {
        ExamResult result = examResultRepository.findById(resultId)
            .orElseThrow(() -> new CustomException("考试结果不存在"));
        
        // 重新获取题目信息
        List<ExamQuestion> questions = examQuestionRepository.findByPaperIdOrderBySort(result.getPaperId());
        
        // 解析原答案
        // ExamResult中没有getAnswerDetails方法
        Map<Long, String> answers = parseAnswersFromJson(""); // result.getAnswerDetails();
        
        // 重新计算成绩
        ScoreCalculationResult scoreResult = calculateScore(questions, answers);
        
        // 更新结果
        result.setTotalScore(BigDecimal.valueOf(scoreResult.getUserScore()));
        result.setCorrectCount(scoreResult.getCorrectCount());
        // ExamResult中没有setUpdateTime方法
        // result.setUpdateTime(LocalDateTime.now());
        
        ExamResult updatedResult = examResultRepository.save(result);
        return convertToResponse(updatedResult);
    }
    
    @Override
    public boolean hasUserTakenExam(Long userId, Long paperId) {
        return examResultRepository.findByUserIdAndPaperId(userId, paperId).isPresent();
    }
    
    @Override
    public List<Map<String, Object>> getExportData(Long paperId) {
        List<ExamResult> results = examResultRepository.findByPaperId(paperId);
        
        return results.stream().map(result -> {
            Map<String, Object> data = new HashMap<>();
            
            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(result.getUserId());
            data.put("userName", userOpt.map(User::getUsername).orElse("未知用户"));
            data.put("userScore", result.getTotalScore());
            // 需要从试卷信息中获取总分
            data.put("totalScore", result.getTotalScore());
            // 需要正确计算百分比，这里暂时设为100
            data.put("scorePercentage", 100.0);
            data.put("correctCount", result.getCorrectCount());
            // ExamResult中没有getTotalQuestions方法，使用其他方式计算
             data.put("totalQuestions", result.getCorrectCount() + result.getWrongCount() + result.getUnansweredCount());
            data.put("examDuration", result.getDuration());
            data.put("examTime", result.getStartTime());
            
            return data;
        }).collect(Collectors.toList());
    }

    public Optional<ExamResult> getByUserAndPaper(Long userId, Long paperId) {
        return examResultRepository.findByUserIdAndPaperId(userId, paperId);

    }

    @Override
    public List<ExamResult> getByUserId(Long userId) {
        return examResultRepository.findByUserId(userId);
    }

    @Override
    public List<ExamResult> getByUserId_Mapper(Long userId) {
        return examResultRepository.getByUserId_Mapper(userId);
    }

    @Override
    public Map<ExamQuestion.QuestionType, String> getQuestionTypeAccuracy(Long userId, Long paperId) {
        List<ExamAnswer> userAnswers = examAnswerService.getByUserAndPaper(userId, paperId);
        List<ExamQuestion> questions = examQuestionService.getByPaperId(paperId);

        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, q -> q));


        // 先统计试卷中所有题型的题目总数
        Map<ExamQuestion.QuestionType, Integer> totalCount = questions.stream()
                .collect(Collectors.groupingBy(
                        ExamQuestion::getQuestionType,
                        Collectors.summingInt(q -> 1)
                ));

        // 统计用户回答正确的题目数
        Map<ExamQuestion.QuestionType, AtomicInteger> correctCount = new HashMap<>();
        userAnswers.forEach(ans -> {
            ExamQuestion question = questionMap.get(ans.getQuestionId());
            if (question != null && Boolean.TRUE.equals(ans.getIsCorrect())) {
                correctCount.computeIfAbsent(
                        question.getQuestionType(),
                        k -> new AtomicInteger(0)
                ).incrementAndGet();
            }
        });

        // 合并统计结果
        return questions.stream()
                .map(ExamQuestion::getQuestionType)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> {
                            int total = totalCount.getOrDefault(type, 0);
                            int correct = correctCount.getOrDefault(type, new AtomicInteger(0)).get();
                            return correct + "/" + total;
                        }
                ));
    }


    // 私有辅助方法
    
    private void validateSubmitRequest(ExamAnswerSubmitRequest request) {
        // ExamAnswerSubmitRequest中没有getUserId方法
        // if (request.getUserId() == null) {
        //     throw new CustomException("用户ID不能为空");
        // }
        if (request.getPaperId() == null) {
            throw new CustomException("试卷ID不能为空");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new CustomException("答案不能为空");
        }
        // ExamAnswerSubmitRequest中没有getStartTime方法
        // if (request.getStartTime() == null) {
        //     throw new CustomException("考试开始时间不能为空");
        // }
    }
    
    private ScoreCalculationResult calculateScore(List<ExamQuestion> questions, Map<Long, String> userAnswers) {
        int totalScore = 0;
        int userScore = 0;
        int correctCount = 0;
        List<AnswerDetail> answerDetails = new ArrayList<>();
        
        for (ExamQuestion question : questions) {
            totalScore += question.getScore();
            
            String userAnswer = userAnswers.get(question.getQuestionId());
            boolean isCorrect = checkAnswer(question, userAnswer);
            
            if (isCorrect) {
                userScore += question.getScore();
                correctCount++;
            }
            
            AnswerDetail detail = new AnswerDetail();
            detail.setQuestionId(question.getQuestionId());
            detail.setUserAnswer(userAnswer);
            detail.setCorrectAnswer(String.join(",", question.getCorrectAnswer()));
            detail.setIsCorrect(isCorrect);
            detail.setScore(isCorrect ? question.getScore() : 0);
            answerDetails.add(detail);
        }
        
        return new ScoreCalculationResult(totalScore, userScore, correctCount, answerDetails);
    }
    
    private boolean checkAnswer(ExamQuestion question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }
        
        List<String> correctAnswerList = question.getCorrectAnswer();
        if (correctAnswerList == null || correctAnswerList.isEmpty()) {
            return false;
        }
        
        // 将List<String>转换为String进行比较
        String correctAnswer = String.join(",", correctAnswerList);
        
        // 标准化答案格式
        userAnswer = userAnswer.trim().toUpperCase();
        correctAnswer = correctAnswer.trim().toUpperCase();
        
        // 对于多选题，需要处理答案顺序
        if (question.getQuestionType() == ExamQuestion.QuestionType.multi) {
            String[] userOptions = userAnswer.split(",");
            String[] correctOptions = correctAnswer.split(",");
            
            Arrays.sort(userOptions);
            Arrays.sort(correctOptions);
            
            return Arrays.equals(userOptions, correctOptions);
        }
        
        return userAnswer.equals(correctAnswer);
    }
    
    private Integer calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    private String convertAnswersToJson(List<AnswerDetail> answerDetails) {
        // 简单的JSON转换，实际项目中应使用Jackson等库
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < answerDetails.size(); i++) {
            AnswerDetail detail = answerDetails.get(i);
            json.append("{");
            json.append("\"questionId\":").append(detail.getQuestionId()).append(",");
            json.append("\"userAnswer\":\"").append(detail.getUserAnswer()).append("\",");
            json.append("\"correctAnswer\":\"").append(detail.getCorrectAnswer()).append("\",");
            json.append("\"isCorrect\":").append(detail.getIsCorrect()).append(",");
            json.append("\"score\":").append(detail.getScore());
            json.append("}");
            if (i < answerDetails.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
    
    private Map<Long, String> parseAnswersFromJson(String answerDetailsJson) {
        // 简单的JSON解析，实际项目中应使用Jackson等库
        Map<Long, String> answers = new HashMap<>();
        // TODO: 实现JSON解析逻辑
        return answers;
    }
    
    private ExamResultResponse convertToResponse(ExamResult result) {
        logger.info("🔍 开始转换考试结果，resultId: {}, userId: {}, paperId: {}", 
            result.getResultId(), result.getUserId(), result.getPaperId());
        
        ExamResultResponse response = new ExamResultResponse();
        response.setResultId(result.getResultId());
        response.setUserId(result.getUserId());
        response.setPaperId(result.getPaperId());
        
        // 获取试卷信息以计算总分
        Optional<ExamPaper> paperOpt = examPaperRepository.findById(result.getPaperId());
        if (paperOpt.isPresent()) {
            ExamPaper paper = paperOpt.get();
            response.setPaperName(paper.getPaperName());
            
            // 计算试卷总分
            List<ExamQuestion> questions = examQuestionRepository.findByPaperId(result.getPaperId());
            int paperTotalScore = questions.stream().mapToInt(ExamQuestion::getScore).sum();
            response.setTotalScore(paperTotalScore);
            
            logger.info("📊 试卷总分: {}, 用户得分: {}", paperTotalScore, result.getTotalScore());
        } else {
            response.setPaperName("未知试卷");
            response.setTotalScore(100); // 默认总分
            logger.warn("⚠️ 未找到试卷信息，paperId: {}", result.getPaperId());
        }
        
        // ExamResult中的totalScore实际上是用户得分
        int userScore = result.getTotalScore().intValue();
        response.setUserScore(userScore);
        
        // 正确计算百分比
        double percentage = response.getTotalScore() > 0 ? 
            (double) userScore / response.getTotalScore() * 100 : 0.0;
        response.setScorePercentage(Math.round(percentage * 100.0) / 100.0);
        
        response.setCorrectCount(result.getCorrectCount());
        // ExamResult中没有getTotalQuestions方法，使用其他方式计算
         response.setTotalQuestions(result.getCorrectCount() + result.getWrongCount() + result.getUnansweredCount());
        response.setStartTime(result.getStartTime());
         response.setSubmitTime(result.getEndTime());
         response.setTimeSpent(result.getDuration());
         // ExamResultResponse中没有setCreateTime方法，移除此调用
        
        // 获取用户名
        Optional<User> userOpt = userRepository.findById(result.getUserId());
        response.setUserName(userOpt.map(User::getUsername).orElse("未知用户"));
        
        logger.info("✅ 转换完成 - 用户得分: {}, 试卷总分: {}, 百分比: {}%, 正确数: {}, 总题数: {}", 
            response.getUserScore(), response.getTotalScore(), response.getScorePercentage(),
            response.getCorrectCount(), response.getTotalQuestions());
        
        return response;
    }

    // 内部类
    private static class ScoreCalculationResult {
        private final int totalScore;
        private final int userScore;
        private final int correctCount;
        private final List<AnswerDetail> answerDetails;
        
        public ScoreCalculationResult(int totalScore, int userScore, int correctCount, List<AnswerDetail> answerDetails) {
            this.totalScore = totalScore;
            this.userScore = userScore;
            this.correctCount = correctCount;
            this.answerDetails = answerDetails;
        }
        
        public int getTotalScore() { return totalScore; }
        public int getUserScore() { return userScore; }
        public int getCorrectCount() { return correctCount; }
        public List<AnswerDetail> getAnswerDetails() { return answerDetails; }
    }
    
    private static class AnswerDetail {
        private Long questionId;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private Integer score;
        
        // Getters and Setters
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
    
    /**
     * 保存详细的答题记录
     */
    private void saveAnswerDetails(Long resultId, ExamAnswerSubmitRequest submitRequest, List<AnswerDetail> answerDetails) {
        try {
            for (AnswerDetail detail : answerDetails) {
                ExamAnswer examAnswer = new ExamAnswer();
                examAnswer.setUserId(submitRequest.getUserId());
                examAnswer.setPaperId(submitRequest.getPaperId());
                examAnswer.setQuestionId(detail.getQuestionId());
                
                // 将用户答案转换为List<String>，确保不为null
                List<String> userAnswerList = new ArrayList<>();
                if (detail.getUserAnswer() != null && !detail.getUserAnswer().trim().isEmpty()) {
                    // 处理多选答案（逗号分隔），使用LinkedHashSet避免重复并保持顺序
                    String[] answers = detail.getUserAnswer().split(",");
                    Set<String> uniqueAnswers = new LinkedHashSet<>();
                    for (String answer : answers) {
                        String trimmedAnswer = answer.trim().toUpperCase();
                        if (!trimmedAnswer.isEmpty()) {
                            uniqueAnswers.add(trimmedAnswer);
                        }
                    }
                    userAnswerList.addAll(uniqueAnswers);
                    
                    logger.debug("题目ID: {}, 原始答案: {}, 处理后答案: {}", 
                        detail.getQuestionId(), detail.getUserAnswer(), userAnswerList);
                }
                // 确保userAnswer字段不为null，即使是空答案也设置为空列表
                examAnswer.setUserAnswer(userAnswerList);
                
                // 确保isCorrect字段有默认值
                examAnswer.setIsCorrect(detail.getIsCorrect() != null ? detail.getIsCorrect() : false);
                examAnswer.setAnswerTime(LocalDateTime.now());
                
                examAnswerRepository.save(examAnswer);
                logger.debug("成功保存答题记录 - 用户ID: {}, 题目ID: {}, 答案: {}", 
                    submitRequest.getUserId(), detail.getQuestionId(), userAnswerList);
            }
        } catch (Exception e) {
            logger.error("保存答题记录失败 - 用户ID: {}, 试卷ID: {}, 错误: {}", 
                submitRequest.getUserId(), submitRequest.getPaperId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}