package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 * 提供基于AI的题目生成功能
 */
public interface IAIService {
    
    /**
     * 基于知识块生成题目
     * @param chunkContent 知识块内容
     * @param questionType 题目类型
     * @param difficulty 难度等级
     * @return 生成的题目数据
     */
    Map<String, Object> generateQuestionFromChunk(String chunkContent, 
                                                  QuestionType questionType, 
                                                  String difficulty);
    
    /**
     * 基于提示词生成多个题目
     * @param prompt 提示词
     * @param questionCount 题目数量
     * @param difficulty 难度等级
     * @return 生成的题目列表
     */
    List<Map<String, Object>> generateQuestionsFromPrompt(String prompt, 
                                                          Integer questionCount, 
                                                          String difficulty);
    
    /**
     * 生成单选题
     * @param content 题目内容基础
     * @param difficulty 难度等级
     * @return 单选题数据
     */
    Map<String, Object> generateSingleChoiceQuestion(String content, String difficulty);
    
    /**
     * 生成多选题
     * @param content 题目内容基础
     * @param difficulty 难度等级
     * @return 多选题数据
     */
    Map<String, Object> generateMultiChoiceQuestion(String content, String difficulty);
    
    /**
     * 生成判断题
     * @param content 题目内容基础
     * @param difficulty 难度等级
     * @return 判断题数据
     */
    Map<String, Object> generateJudgeQuestion(String content, String difficulty);
    
    /**
     * 解析AI响应
     * @param aiResponse AI返回的响应
     * @param questionType 题目类型
     * @return 解析后的题目数据
     */
    List<Map<String, Object>> parseAIResponse(String aiResponse, String questionType);
    
    /**
     * 调用AI模型
     * @param messages 消息列表
     * @return AI响应
     */
    String callAIModel(List<Map<String, String>> messages);
    
    /**
     * 构建系统提示词
     * @param questionType 题目类型
     * @param difficulty 难度等级
     * @return 系统提示词
     */
    String buildSystemPrompt(String questionType, String difficulty);
    
    /**
     * 验证题目数据
     * @param questionData 题目数据
     * @return 是否有效
     */
    boolean validateQuestionData(Map<String, Object> questionData);
    
    /**
     * 格式化选项
     * @param options 选项列表
     * @return 格式化后的选项字符串
     */
    String formatOptions(List<String> options);
    
    /**
     * 提取正确答案
     * @param questionData 题目数据
     * @param questionType 题目类型
     * @return 正确答案
     */
    String extractCorrectAnswer(Map<String, Object> questionData, QuestionType questionType);
}