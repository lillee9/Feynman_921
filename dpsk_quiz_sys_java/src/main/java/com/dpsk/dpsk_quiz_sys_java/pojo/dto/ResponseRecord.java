package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 结构化响应记录  用于记录ai对话的消息历史
 */

public class ResponseRecord {
    private final String id;
    private final int type; // 0-AI回答 / 1-用户提问
    private final long timestamp;
    private final String content;
    private final String rawData; // 原始数据

    // 类型常量
    public static final int TYPE_AI_RESPONSE = 0;
    public static final int TYPE_USER_QUERY = 1;

    public ResponseRecord(String id, int type, long timestamp, String content, String rawData) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.content = content;
        this.rawData = rawData;
    }

    // Getters
    public String getId() { return id; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getContent() { return content; }
    public String getRawData() { return rawData; }

    public String getFormattedTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .toString();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | ID: %s\nContent: %s\nRaw: %s",
                getFormattedTime(),
                type == TYPE_AI_RESPONSE ? "AI Response" : "User Query",
                id,
                content,
                rawData);
    }
}
