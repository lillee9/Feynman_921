package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.dto.*;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamPaper;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;

import java.util.Arrays;
import java.util.Set;

/**
 * 考试试卷服务接口
 */
public interface IExamPaperService {
    
    /**
     * 基于知识库创建试卷
     */
    ExamPaperResponse createPaperFromKnowledgeBase(ExamPaperCreateRequest request, Long creatorId);
    
    /**
     * 基于大模型直接生成试卷
     */
    ExamPaperResponse createPaperFromAI(ExamPaperCreateRequest request, Long creatorId, String prompt);
    
    /**
     * 创建试卷并保存已生成的题目
     */
    ExamPaperResponse createPaperWithQuestions(ExamPaperCreateRequest request, Long creatorId);
    
    /**
     * 获取试卷详情
     */
    ExamPaperResponse getPaper(Long paperId);
    
    /**
     * 分页查询试卷列表
     */
    PageResponse<ExamPaperResponse> getPapers(PageRequest pageRequest);
    
    /**
     * 更新试卷信息
     */
    ExamPaperResponse updatePaper(Long paperId, ExamPaperCreateRequest request);
    
    /**
     * 删除试卷
     */
    void deletePaper(Long paperId);
    
    /**
     * 发布试卷
     */
    ExamPaperResponse publishPaper(Long paperId);
    
    /**
     * 取消发布试卷
     */
    ExamPaperResponse unpublishPaper(Long paperId);
    
    /**
     * 获取用户创建的试卷列表
     */
    PageResponse<ExamPaperResponse> getUserPapers(Long creatorId, PageRequest pageRequest);
    
    /**
     * 获取可参与的试卷列表（已发布且未过期）
     */
    PageResponse<ExamPaperResponse> getAvailablePapers(PageRequest pageRequest);
    
    /**
     * 复制试卷
     */
    ExamPaperResponse copyPaper(Long paperId, Long creatorId, String newPaperName);

    /**
     * 根据ID获取试卷
     */
    ExamPaper getById(Long paperId);

    ExamPaper getById_Mapper(Long paperId);

    Set<ExamQuestion.QuestionType> getQuestionTypes(Long paperId);
}