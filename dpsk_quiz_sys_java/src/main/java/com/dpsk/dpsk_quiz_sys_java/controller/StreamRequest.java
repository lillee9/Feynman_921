package com.dpsk.dpsk_quiz_sys_java.controller;

public class StreamRequest {
    private String messages;
    private Integer conversationId;

    public StreamRequest() {
    }

    public StreamRequest(String messages, Integer conversationId) {
        this.messages = messages;
        this.conversationId = conversationId;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String toString() {
        return "StreamRequest{" +
                "messages='" + messages + '\'' +
                ", conversationId=" + conversationId +
                '}';
    }
}