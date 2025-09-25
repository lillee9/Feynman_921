package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Conversation;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.service.ConversationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/conversation")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/getAll")
    public ResponseMessage<List<Conversation>> getAllConversation(@RequestParam Long userId) {
        List<Conversation> res = conversationService.getAll(userId);
        return ResponseMessage.success(res);
    }

    @PostMapping("/add")
    public ResponseMessage<Conversation> addConversation(@RequestBody Conversation conversation) {
        Conversation res = conversationService.add(conversation);
        return ResponseMessage.success(res);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseMessage<Conversation> deleteConversation(@PathVariable Integer conversationId) {
        conversationService.delete(conversationId);
        return ResponseMessage.success();
    }

    @PostMapping("/edit")
    public ResponseMessage editConversation(@RequestBody Conversation conversation) {
        if(conversation.getConversationId() == null) {
            return ResponseMessage.error("不可编辑空对话");
        }
        conversationService.edit(conversation);
        return ResponseMessage.success();
    }
}
