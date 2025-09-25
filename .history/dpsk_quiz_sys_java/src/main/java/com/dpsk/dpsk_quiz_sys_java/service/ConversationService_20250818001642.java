package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Conversation;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation add(Conversation conversation){
        Conversation conversation1 = conversationRepository.save(conversation);
        return conversation1;
    }

    public List<Conversation> getAll(Long userId) {
        return conversationRepository.findByUserId(userId);
    }

    public void delete(Integer conversationId) {
         conversationRepository.deleteById(conversationId);
    }

    public void edit(Conversation conversation) {
        conversationRepository.updateTitle(conversation.getTitle(),conversation.getConversationId());
    }
}
