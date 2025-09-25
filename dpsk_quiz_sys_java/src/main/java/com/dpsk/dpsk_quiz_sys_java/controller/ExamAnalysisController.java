package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.AiResult;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import com.dpsk.dpsk_quiz_sys_java.service.*;
import com.dpsk.dpsk_quiz_sys_java.utils.JsonUtils;
import okhttp3.*;
import okhttp3.RequestBody;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


// 已补充完整的导入、方法实现及注释
@RestController
@CrossOrigin()
@RequestMapping("/exam-analysis")
public class ExamAnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(ExamAnalysisController.class);
    private static final String DONE = "[DONE]";
    private static final Integer timeout = 60;
    private static final String AI_URL = "https://api.deepseek.com/chat/completions";

    @Value("${api.password:}")
    private String apiPassword;

    @Autowired
    private ExamPaperService examPaperService;  // 试卷表Service
    @Autowired
    private ExamQuestionService examQuestionService;  // 试题表Service
    @Autowired
    private ExamAnswerService examAnswerService;  // 答题记录表Service
    @Autowired
    private ExamResultService examResultService;  // 考试结果表Service

    @Autowired
    private AnalysisRecordService analysisRecordService;

    @PostMapping("/save")
    public ResponseEntity<?> saveAnalysisRecord(
            @org.springframework.web.bind.annotation.RequestBody AnalysisRecordDto dto) {  // 改为@RequestBody接收JSON对象

        // 参数校验（直接校验dto中的字段）
        if (dto.getUserId() == null || dto.getPaperId() == null) {
            return ResponseEntity.badRequest().body("用户ID和试卷ID不能为空");
        }
        if (dto.getAnalysisData() == null || dto.getAdviceData() == null) {
            return ResponseEntity.badRequest().body("分析数据和建议数据不能为空");
        }
        Long userId = dto.getUserId();
        Long paperId = dto.getPaperId();
        // 调用Service保存（逻辑不变）
        try {
            // 检查是否存在旧记录
            boolean exists = analysisRecordService.existsByUserAndPaper(userId, paperId);
            if (exists) {
                // 删除旧记录
                analysisRecordService.deleteByUserAndPaper(userId, paperId);
                logger.info("删除已存在的分析记录，用户ID：{}，试卷ID：{}", userId, paperId);
            }
            analysisRecordService.saveAnalysisRecord(dto);
            return ResponseEntity.ok("分析记录保存成功");
        } catch (Exception e) {
            logger.error("保存分析记录失败，用户ID：{}，试卷ID：{}", dto.getUserId(), dto.getPaperId(), e);
            return ResponseEntity.internalServerError().body("保存失败：" + e.getMessage());
        }
    }
    // 新增方法1：验证是否可进行分析
    @GetMapping("/makeSure")
    public ResponseEntity<?> checkAnalysisCondition(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "paperId") Long paperId) {
        // 参数校验
        if (userId == null || paperId == null) {
            return ResponseEntity.badRequest().body("用户ID和试卷ID不能为空");
        }

        // 业务逻辑：检查是否存在考试结果且未生成过分析记录（示例逻辑）
        boolean hasAnalysisRecord = analysisRecordService.existsByUserAndPaper(userId, paperId);

        // 返回验证结果（假设前端需要布尔值判断是否允许分析）
        return ResponseEntity.ok(new CheckAnalysisDto(hasAnalysisRecord));
    }

    // 新增方法2：获取已生成的分析报告
    @GetMapping("/get")
    public ResponseEntity<?> getAnalysisResult(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "paper_id") Long paperId) {
        // 参数校验
        if (userId == null || paperId == null) {
            return ResponseEntity.badRequest().body("用户ID和试卷ID不能为空");
        }

        // 查询分析记录（假设analysisRecordService有按用户+试卷查询的方法）
        Optional<AnalysisRecord> recordOpt = analysisRecordService.getByUserAndPaper(userId, paperId);
        if (recordOpt.isEmpty()) {
            // 修复：使用status(HttpStatus.NOT_FOUND)代替notFound()以支持body()
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该用户的试卷分析记录");
        }


        // 转换为DTO返回（假设AnalysisRecordDto有对应构造方法）
        AnalysisRecordDto result = new AnalysisRecordDto(recordOpt.get());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/papers")
    public ResponseEntity<?> getPapersByUserId(@RequestParam Long userId) {
        // 参数校验
        if (userId == null) {
            return ResponseEntity.badRequest().body("用户ID不能为空");
        }

        // 查询用户所有考试结果（假设examResultService有getByUserId方法）
        List<ExamResult> examResults = examResultService.getByUserId_Mapper(userId);
        if (examResults.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 组装试卷数据（关联查询试卷名称）
        List<PaperDto> result = examResults.stream()
                .map(examResult -> {
                    // 根据paperId查询试卷信息
                    ExamPaper examPaper = examPaperService.getById_Mapper(examResult.getPaperId());
                    if (examPaper == null) {
                        logger.warn("试卷ID {} 不存在", examResult.getPaperId());
                        return null;
                    }
                    // 组装返回对象（字段对应需与实际表结构匹配）
                    return new PaperDto(
                            examResult.getPaperId(),
                            examPaper.getPaperName(),
                            examResult.getPaperDifficulty(),  // 假设exam_result表包含该字段
                            examResult.getTotalScore(),       // 假设exam_result表包含该字段
                            examResult.getEndTime(),
                            examResult.getDuration(),         // 假设exam_result表包含该字段
                            null                              // questionTypes暂时设为null
                    );
                })
                .filter(Objects::nonNull)  // 过滤不存在的试卷
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/score")
    public ResponseEntity<?> getExamScore(
            @RequestParam Long userId,
            @RequestParam Long paperId) {
        // 参数校验
        if (userId == null || paperId == null) {
            return ResponseEntity.badRequest().body("用户ID和试卷ID不能为空");
        }

        // 查询考试结果
        Optional<ExamResult> examResultOpt = examResultService.getByUserAndPaper(userId, paperId);
        if (examResultOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ExamResult examResult = examResultOpt.get();
        // 构造返回对象（假设ExamResult包含score和duration字段）
        ExamPaper examPaper = examPaperService.getById_Mapper(paperId);

        // 新增：获取题型准确率数据
         Map<QuestionType, String> accuracyMap =
                 examResultService.getQuestionTypeAccuracy(userId, paperId);

        // 修改返回对象为ExamAnalysisResponse
        ExamAnalysisResponse response = new ExamAnalysisResponse();
        // 计算总准确率：(正确题数 / 总题数) * 100，四舍五入取整
        int correctCount = examResult.getCorrectCount();
        int totalQuestion = correctCount + examResult.getWrongCount() + examResult.getUnansweredCount();
        int accuracy = totalQuestion == 0 ? 0 : (int) Math.round((correctCount * 100.0) / totalQuestion);
        response.setTotalScore(BigDecimal.valueOf(accuracy));
        response.setDuration(examResult.getDuration());

        // 新增：转换题型数据
        List<ExamAnalysisResponse.QuestionTypeAccuracy> typeAccuracies = examPaperService.getQuestionTypes(paperId)
                .stream()
                .map(type -> {
                    ExamAnalysisResponse.QuestionTypeAccuracy dto = new ExamAnalysisResponse.QuestionTypeAccuracy(type.getDescription(), accuracyMap.getOrDefault(type, "0/0"));
                     dto.setTypeName(type.getDescription());
                    dto.setAccuracy(accuracyMap.getOrDefault(type, "0/0"));
                    return dto;
                })
                .collect(Collectors.toList());
        response.setQuestionTypes(typeAccuracies);

        return ResponseEntity.ok(response);
    }
    /**
     * 考试分析主接口（SSE流式输出）
     */
    @GetMapping("/analyze")
    public void analyzeExam(
            @RequestParam Long userId,
            @RequestParam Long paperId,
            @RequestParam boolean flag,
            HttpServletResponse response) throws IOException {
        // 参数校验
        if (userId == null || paperId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("用户ID或试卷ID不能为空");
            return;
        }

        // 数据库查询
        ExamPaper examPaper = examPaperService.getById(paperId);
        if (examPaper == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("试卷不存在");
            return;
        }

        Optional<ExamResult> examResultOptional = examResultService.getByUserAndPaper(userId, paperId);
        if (examResultOptional.isEmpty()) {  // 替换原有的 examResult == null
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("用户未参与该试卷");
            return;
        }

         // 修改点2：从 Optional 中提取实际的 ExamResult 对象
        ExamResult examResult = examResultOptional.get();

        List<ExamAnswer> userAnswers = examAnswerService.getByUserAndPaper(userId, paperId);
        List<ExamQuestion> questions = examQuestionService.getByPaperId(paperId);
//

        // 核心分析逻辑
        ExamAnalysisResult analysisResult = analyzeUserAnswers(examPaper, userAnswers, examResult, questions);

        // SSE响应配置
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 调用AI生成报告
        try (PrintWriter pw = response.getWriter()) {
            generateAnalysisReport(pw, analysisResult,flag,userAnswers,examResult,questions);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (InterruptedException e) {
            logger.error("分析过程中发生中断", e);
            throw new RuntimeException("分析过程中发生中断", e);
        }
    }

    /**
     * 核心分析逻辑（统计错题、薄弱知识点、章节正确率）
     */
    private ExamAnalysisResult analyzeUserAnswers(ExamPaper examPaper,
                                                  List<ExamAnswer> userAnswers,
                                                  ExamResult examResult,
                                                  List<ExamQuestion> questions) {
        questions.forEach(question -> {
            // 强制初始化Hibernate代理
            Hibernate.initialize(question.getExamPaper());
            Hibernate.initialize(question.getKbChunk());
        });
        ExamAnalysisResult result = new ExamAnalysisResult();

        // 统计错题
        List<WrongQuestion> wrongQuestions = userAnswers.stream()
                .filter(ans -> ans.getIsCorrect() != null && !ans.getIsCorrect())
                .map(ans -> {
                    ExamQuestion question = questions.stream()
                            .filter(q -> q.getQuestionId().equals(ans.getQuestionId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("试题信息缺失"));

                    // 获取章节和知识点信息
                    String chapter = question.getKbChunk() != null ? question.getKbChunk().getChapter() : "未知章节";
                    String knowledgePoint = question.getKbChunk() != null ? question.getKbChunk().getChapterName() : "未知知识点";
                    
                    return new WrongQuestion(
                            ans.getQuestionId(),
                            question.getQuestionType(),  // 类型：QuestionType
                            chapter,                     // 章节信息
                            knowledgePoint,              // 知识点信息
                            ans.getUserAnswer(),         // 类型：List<String>（用户答案）
                            question.getCorrectAnswer(), // 类型：List<String>（正确答案）
                            ans.getAnswerTime()          // 类型：LocalDateTime（答题时间）
                    );
                })
                .collect(Collectors.toList());

        result.setWrongQuestions(wrongQuestions);

        // 计算基础指标（修改后）
        Integer totalQuestion = examResult.getCorrectCount() + examResult.getWrongCount() + examResult.getUnansweredCount();
        result.setTotalQuestion(totalQuestion);  // 设置总题数（通过现有字段计算）
        result.setCorrectCount(examResult.getCorrectCount());
        result.setWrongCount(examResult.getWrongCount());
        result.setUnansweredCount(examResult.getUnansweredCount());
        result.setCorrectRate((double) examResult.getCorrectCount() / totalQuestion);  // 使用计算的总题数
        result.setTotalCostTime(examResult.getDuration());

        // 统计薄弱知识点
        Map<String, Integer> knowledgeCount = new HashMap<>();
        wrongQuestions.forEach(wq ->
                knowledgeCount.put(wq.getKnowledgePoint(), knowledgeCount.getOrDefault(wq.getKnowledgePoint(), 0) + 1)
        );
        result.setWeakKnowledgePoints(knowledgeCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        // 按题型统计准确率（修改后）
        // 1. 将试题按 questionId 缓存，方便快速查找题型
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, q -> q));

        // 2. 按题型分组，统计总题数和正确题数
         Map<QuestionType, Long> totalCountByType = new HashMap<>();
         Map<QuestionType, Long> correctCountByType = new HashMap<>();

        userAnswers.forEach(ans -> {
            ExamQuestion question = questionMap.get(ans.getQuestionId());
            if (question == null) return;

            QuestionType type = question.getQuestionType();
            // 总题数+1
            totalCountByType.put(type, totalCountByType.getOrDefault(type, 0L) + 1);
            // 正确题数+1（仅当答题正确时）
            if (ans.getIsCorrect() != null && ans.getIsCorrect()) {
                correctCountByType.put(type, correctCountByType.getOrDefault(type, 0L) + 1);
            }
        });

        // 3. 计算每个题型的准确率（正确数/总题数）
         Map<QuestionType, Double> questionTypeCorrectRate = new HashMap<>();
         for (Map.Entry<QuestionType, Long> entry : totalCountByType.entrySet()) {
             QuestionType type = entry.getKey();
            Long total = entry.getValue();
            Long correct = correctCountByType.getOrDefault(type, 0L);
            double rate = total == 0 ? 0.0 : (double) correct / total;
            questionTypeCorrectRate.put(type, rate);
        }

        result.setQuestionTypeCorrectRate(questionTypeCorrectRate);  // 假设 ExamAnalysisResult 有该字段的 setter

        return result;
    }

    /**
     * 调用AI生成分析报告（SSE流式处理）
     */
    private void generateAnalysisReport(PrintWriter pw, ExamAnalysisResult analysisResult,boolean flag,List<ExamAnswer> userAnswers,ExamResult examResult,List<ExamQuestion> questions) throws InterruptedException {


        // 构造对话消息（优化后的prompt模板）
        List<Map<String, String>> messages = new ArrayList<>();
        // system提示：明确角色、输出结构、内容要求和格式约束
        String systemContent = "你是教育分析专家，需根据用户考试数据生成结构化学习报告。输出规则：\n" +
                "1. 必须包含且仅包含两部分内容，每个部分的内容控制到3-4条建议，且不能出现口语化用词\n" +
                "2. 第一部分标题为【用户欠缺知识点分析】，需按以下格式输出：\n" +
                "   1 知识点名称1：具体错误原因（如：对XX概念理解模糊）\n" +
                "   2 知识点名称2：具体错误原因\n" +
                "3. 第二部分标题为【用户建议】，需按以下格式输出：\n" +
                "   1 针对知识点名称1：具体建议（加强某专业知识的的学习，如：多做相关练习题）\n" +
                "   2 针对知识点名称2：具体建议（某方面做的不错，继续保持）\n" +
                "   （与【用户欠缺知识点分析】中的知识点顺序一一对应）\n" +
                "4. 标题后直接跟分点内容，不添加任何其他文字、空行或标点";

        // 当需要重新分析时，在系统提示前添加深入思考要求
        if(flag) {
            systemContent = "【深度分析模式】请仔细思考以下要求后执行任务：\n" +
                    "1. 上次分析结果不符合预期，每个部分的内容控制到3-4条建议，且不能出现口语化用词，需要更深入思考以下方面：\n" +
                    "   - 错误原因是否触及本质（概念理解/计算能力/思维方法）\n" +
                    "   - 建议是否具体可操作（加强某专业知识的学习）\n" +
                    "   - 是否挖掘了知识点间的关联性\n" +
                    "2. 重新检查数据中的异常模式（如：\n" +
                    "   - 同一知识点的不同题型表现差异\n" +
                    "3. 分析完成后，严格按以下格式输出：\n\n" +
                    systemContent;
        }

        messages.add(Map.of("role", "system", "content", systemContent));
        // user提示：提供完整考试数据并强调格式要求
        messages.add(Map.of("role", "user", "content",
                "用户考试数据如下（包含错题信息、薄弱知识点列表、各题型准确率等）：\n" +
                        analysisResult.toString() + "\n" +
                        "请严格按照system指令的格式要求输出，禁止添加任何标题外的内容"));

        // 构造题目详细信息
        StringBuilder questionDetails = new StringBuilder("\n\n题目详细信息：");

        // 遍历所有题目构建详细信息
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            ExamAnswer answer = userAnswers.stream()
                    .filter(a -> a.getQuestionId().equals(question.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            questionDetails.append(String.format(
                    "\n第%d题：\n题干：%s\n正确答案：%s\n用户答案：%s",
                    i + 1,
                    question.getQuestionContent(),
                    question.getCorrectAnswer(),
                    (answer != null) ? answer.getUserAnswer() : "未作答"
            ));
        }

        // 修改用户提示内容
        messages.add(Map.of("role", "user", "content",
                "用户考试数据如下（包含错题信息、薄弱知识点列表、各题型准确率等）：\n" +
                        analysisResult.toString() +
                        questionDetails.toString() + "\n" +  // 添加题目详细信息
                        "请严格按照system指令的格式要求输出，结合具体题目内容进行分析"));
        // ... 以下构造请求参数、发送HTTP请求等代码保持不变 ...
        // 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("model", "deepseek-chat");
        params.put("stream", true);
        params.put("messages", messages);
        String jsonParams = JsonUtils.convertObj2Json(params);

        // 发送HTTP请求
        Request request = new Request.Builder()
                .url(AI_URL)
                .addHeader("Authorization", "Bearer " + apiPassword)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams))
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();

        // 处理SSE响应
        CountDownLatch eventLatch = new CountDownLatch(1);
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (DONE.equals(data)) return;
                String content = getContent(data);
                if (content != null && !content.isEmpty()) {
                    pw.write("data:" + JsonUtils.convertObj2Json(new AnalysisContentDto(content)) + "\n\n");
                    pw.flush();
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                logger.info("AI分析报告生成完成");
                eventLatch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                logger.error("AI调用失败", t);
                pw.write("data:{\"error\":\"AI分析失败: " + t.getMessage() + "\"}\n\n");
                pw.flush();
                eventLatch.countDown();
            }
        });

        realEventSource.connect(client);
        eventLatch.await();
    }

    private static String getContent(String data) {
        AiResult aiResult = JsonUtils.convertJson2Obj(data, AiResult.class);
        return aiResult.getChoices().get(0).getDelta().getContent();
    }
}