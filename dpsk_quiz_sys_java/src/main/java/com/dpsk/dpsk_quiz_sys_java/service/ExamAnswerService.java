package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamAnswer;


import java.util.List;

public interface ExamAnswerService {
    /**
     * 获取用户在指定试卷下的所有答题记录
     * @param userId 用户ID
     * @param paperId 试卷ID
     * @return 答题记录列表
     */
    List<ExamAnswer> getByUserAndPaper(Long userId, Long paperId);

    /**
     * 获取指定试题的所有答题记录（用于统计正确率）
     * @param questionId 试题ID
     * @return 答题记录列表
     */
    List<ExamAnswer> getByQuestionId(Long questionId);

    /**
     * 获取用户对某道题的答题记录（判断是否已答）
     * @param userId 用户ID
     * @param questionId 试题ID
     * @return 单条答题记录
     */
    ExamAnswer getByUserAndQuestion(Long userId, Long questionId);
}