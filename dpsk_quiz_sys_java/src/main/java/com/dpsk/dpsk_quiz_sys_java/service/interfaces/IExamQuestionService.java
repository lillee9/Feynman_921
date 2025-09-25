package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;

import java.util.List;

/**
 * 考试题目服务接口
 */
public interface IExamQuestionService {
    
    /**
     * 创建题目
     * @param request 题目创建请求
     * @return 题目响应
     */
    ExamQuestionResponse createQuestion(ExamQuestionCreateRequest request);
    
    /**
     * 基于知识块内容生成题目
     * @param request 题目创建请求
     * @param chunkContent 知识块内容
     * @return 题目响应
     */
    ExamQuestionResponse createQuestionFromChunk(ExamQuestionCreateRequest request, String chunkContent);
    
    /**
     * 获取题目详情
     * @param questionId 题目ID
     * @return 题目响应
     */
    ExamQuestionResponse getQuestion(Long questionId);
    
    /**
     * 获取试卷的所有题目
     * @param paperId 试卷ID
     * @param showAnswer 是否显示答案
     * @return 题目列表
     */
    List<ExamQuestionResponse> getQuestionsByPaper(Long paperId, boolean showAnswer);
    
    /**
     * 更新题目
     * @param questionId 题目ID
     * @param request 题目更新请求
     * @return 题目响应
     */
    ExamQuestionResponse updateQuestion(Long questionId, ExamQuestionCreateRequest request);
    
    /**
     * 删除题目
     * @param questionId 题目ID
     */
    void deleteQuestion(Long questionId);
    
    /**
     * 批量删除试卷的题目
     * @param paperId 试卷ID
     */
    void deleteQuestionsByPaper(Long paperId);
    
    /**
     * 获取题目总数
     * @param paperId 试卷ID
     * @return 题目总数
     */
    long getQuestionCount(Long paperId);
    
    /**
     * 调整题目顺序
     * @param questionId 题目ID
     * @param newSort 新的排序值
     */
    void updateQuestionSort(Long questionId, Integer newSort);

    List<ExamQuestion> getByPaperId(Long paperId);
}