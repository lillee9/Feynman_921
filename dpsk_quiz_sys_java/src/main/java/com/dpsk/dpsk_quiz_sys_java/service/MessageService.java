package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Message;
import com.dpsk.dpsk_quiz_sys_java.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void add(Message message) {
        messageRepository.save(message);
    }

    public List<Message> findByConversationId(Integer conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }
}
