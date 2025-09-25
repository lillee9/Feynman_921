package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import com.dpsk.dpsk_quiz_sys_java.repository.ExamPaperRepository;
import com.dpsk.dpsk_quiz_sys_java.repository.ExamQuestionRepository;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 测试数据控制器 - 用于创建测试试卷数据
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, allowCredentials = "true")
public class TestDataController {
    
    @Autowired
    private ExamPaperRepository examPaperRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    /**
     * 创建测试试卷数据
     */
    @PostMapping("/create-sample-papers")
    public ResponseEntity<ResponseMessage<String>> createSamplePapers() {
        try {
            // 创建测试试卷1
            ExamPaper paper1 = new ExamPaper();
            paper1.setPaperName("Java基础知识测试");
            paper1.setCreatorId(1L);
            paper1.setDifficulty(ExamPaper.Difficulty.medium);
            paper1.setTotalScore(100);
            paper1.setTotalQuestion(10);
            paper1.setStatus(ExamPaper.PaperStatus.published);
            paper1.setGenerateTime(LocalDateTime.now().minusDays(1));
            paper1.setExpireTime(LocalDateTime.now().plusDays(30));
            
            // 设置章节范围
            List<String> chapterScope1 = Arrays.asList("第1章 Java概述", "第2章 面向对象编程", "第3章 异常处理");
            paper1.setChapterScope(chapterScope1);
            
            // 设置题目配置
            Map<String, Integer> questionConfig1 = new HashMap<>();
            questionConfig1.put("单选题", 6);
            questionConfig1.put("多选题", 2);
            questionConfig1.put("判断题", 2);
            paper1.setQuestionConfig(questionConfig1);
            
            ExamPaper savedPaper1 = examPaperRepository.save(paper1);
            
            // 为试卷1创建测试题目
            createSampleQuestions(savedPaper1.getPaperId());
            
            // 创建测试试卷2
            ExamPaper paper2 = new ExamPaper();
            paper2.setPaperName("数据库原理与应用");
            paper2.setCreatorId(1L);
            paper2.setDifficulty(ExamPaper.Difficulty.hard);
            paper2.setTotalScore(120);
            paper2.setTotalQuestion(12);
            paper2.setStatus(ExamPaper.PaperStatus.published);
            paper2.setGenerateTime(LocalDateTime.now().minusDays(2));
            paper2.setExpireTime(LocalDateTime.now().plusDays(25));
            
            List<String> chapterScope2 = Arrays.asList("第1章 数据库概述", "第2章 关系数据库", "第3章 SQL语言");
            paper2.setChapterScope(chapterScope2);
            
            Map<String, Integer> questionConfig2 = new HashMap<>();
            questionConfig2.put("单选题", 8);
            questionConfig2.put("多选题", 2);
            questionConfig2.put("简答题", 2);
            paper2.setQuestionConfig(questionConfig2);
            
            ExamPaper savedPaper2 = examPaperRepository.save(paper2);
            createSampleQuestions(savedPaper2.getPaperId());
            
            // 创建测试试卷3
            ExamPaper paper3 = new ExamPaper();
            paper3.setPaperName("前端开发技术");
            paper3.setCreatorId(1L);
            paper3.setDifficulty(ExamPaper.Difficulty.easy);
            paper3.setTotalScore(80);
            paper3.setTotalQuestion(8);
            paper3.setStatus(ExamPaper.PaperStatus.draft);
            paper3.setGenerateTime(LocalDateTime.now().minusHours(6));
            
            List<String> chapterScope3 = Arrays.asList("第1章 HTML基础", "第2章 CSS样式", "第3章 JavaScript编程");
            paper3.setChapterScope(chapterScope3);
            
            Map<String, Integer> questionConfig3 = new HashMap<>();
            questionConfig3.put("单选题", 5);
            questionConfig3.put("判断题", 3);
            paper3.setQuestionConfig(questionConfig3);
            
            ExamPaper savedPaper3 = examPaperRepository.save(paper3);
            createSampleQuestions(savedPaper3.getPaperId());
            
            return ResponseEntity.ok(new ResponseMessage<>(200, "测试试卷数据创建成功", "已创建3份测试试卷"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "创建测试数据失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 为指定试卷创建测试题目
     */
    private void createSampleQuestions(Long paperId) {
        // 创建一些示例题目
        ExamQuestion question1 = new ExamQuestion();
        question1.setPaperId(paperId);
        question1.setQuestionType(QuestionType.SINGLE_CHOICE);
        question1.setQuestionContent("Java是哪种类型的编程语言？");
        question1.setOptions(Arrays.asList("面向过程", "面向对象", "函数式", "逻辑式"));
        question1.setCorrectAnswer(Arrays.asList("面向对象"));
        question1.setScore(10);
        question1.setAnalysis("Java是一种面向对象的编程语言，支持封装、继承和多态等特性。");
        question1.setSort(1);
        examQuestionRepository.save(question1);
        
        ExamQuestion question2 = new ExamQuestion();
        question2.setPaperId(paperId);
        question2.setQuestionType(QuestionType.MULTI_CHOICE);
        question2.setQuestionContent("以下哪些是Java的特点？");
        question2.setOptions(Arrays.asList("跨平台", "面向对象", "安全性高", "编译执行"));
        question2.setCorrectAnswer(Arrays.asList("跨平台", "面向对象", "安全性高"));
        question2.setScore(15);
        question2.setAnalysis("Java具有跨平台、面向对象、安全性高等特点，但Java是解释执行的。");
        question2.setSort(2);
        examQuestionRepository.save(question2);
        
        ExamQuestion question3 = new ExamQuestion();
        question3.setPaperId(paperId);
        question3.setQuestionType(QuestionType.JUDGE);
        question3.setQuestionContent("Java程序需要先编译成字节码才能运行。");
        question3.setOptions(Arrays.asList("正确", "错误"));
        question3.setCorrectAnswer(Arrays.asList("正确"));
        question3.setScore(5);
        question3.setAnalysis("Java程序需要先通过javac编译成.class字节码文件，然后由JVM解释执行。");
        question3.setSort(3);
        examQuestionRepository.save(question3);
    }
    
    /**
     * 清除所有测试数据
     */
    @DeleteMapping("/clear-test-data")
    public ResponseEntity<ResponseMessage<String>> clearTestData() {
        try {
            examQuestionRepository.deleteAll();
            examPaperRepository.deleteAll();
            return ResponseEntity.ok(new ResponseMessage<>(200, "测试数据清除成功", "所有测试数据已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "清除测试数据失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 检查数据库连接状态
     */
    @GetMapping("/check-database")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> checkDatabase() {
        try {
            long paperCount = examPaperRepository.count();
            long questionCount = examQuestionRepository.count();
            
            Map<String, Object> result = new HashMap<>();
            result.put("paperCount", paperCount);
            result.put("questionCount", questionCount);
            result.put("databaseStatus", "connected");
            
            return ResponseEntity.ok(new ResponseMessage<>(200, "数据库连接正常", result));
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("databaseStatus", "error");
            result.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(new ResponseMessage<>(400, "数据库连接异常", result));
        }
    }
}