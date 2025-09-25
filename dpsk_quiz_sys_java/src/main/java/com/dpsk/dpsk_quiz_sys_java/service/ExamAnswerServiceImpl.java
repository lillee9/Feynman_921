package com.dpsk.dpsk_quiz_sys_java.service;


import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamAnswer;
import com.dpsk.dpsk_quiz_sys_java.repository.ExamAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAnswerServiceImpl implements ExamAnswerService {

    @Autowired
    private ExamAnswerRepository examAnswerRepository;

    @Override
    public List<ExamAnswer> getByUserAndPaper(Long userId, Long paperId) {
        return examAnswerRepository.findByUserIdAndPaperId(userId, paperId);
    }

    @Override
    public List<ExamAnswer> getByQuestionId(Long questionId) {
        return examAnswerRepository.findByQuestionId(questionId);
    }

    @Override
    public ExamAnswer getByUserAndQuestion(Long userId, Long questionId) {
        return examAnswerRepository.findByUserIdAndQuestionId(userId, questionId);
    }
}