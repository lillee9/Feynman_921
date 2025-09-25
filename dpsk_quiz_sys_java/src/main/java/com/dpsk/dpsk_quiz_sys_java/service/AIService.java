package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.ExamQuestion;
import com.dpsk.dpsk_quiz_sys_java.pojo.enums.QuestionType;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IAIService;
import com.dpsk.dpsk_quiz_sys_java.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI服务实现类
 */
@Service
public class AIService implements IAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    // AI模型配置
    private static final String AI_API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    @Value("${api.password}")
    private String AI_API_KEY;
    
    @Override
    public Map<String, Object> generateQuestionFromChunk(String chunkContent, QuestionType questionType, String difficulty) {
        try {
            String systemPrompt = buildSystemPrompt(questionType.name(), difficulty);
            String userPrompt = "请基于以下内容生成一道" + getQuestionTypeName(questionType.name()) + "：\n" + chunkContent;
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", systemPrompt));
            messages.add(createMessage("user", userPrompt));
            
            String aiResponse = callAIModel(messages);
            List<Map<String, Object>> questions = parseAIResponse(aiResponse, questionType.name());
            
            return questions.isEmpty() ? createFallbackQuestion(chunkContent, questionType) : questions.get(0);
        } catch (Exception e) {
            logger.error("AI生成题目失败", e);
            return createFallbackQuestion(chunkContent, questionType);
        }
    }
    
    @Override
    public List<Map<String, Object>> generateQuestionsFromPrompt(String prompt, Integer questionCount, String difficulty) {
        List<Map<String, Object>> allQuestions = new ArrayList<>();
        
        try {
            // 默认生成单选题
            String questionType = "SINGLE_CHOICE";
            String systemPrompt = buildSystemPrompt(questionType, difficulty);
            String userPrompt = String.format("请基于以下主题生成%d道%s：\n%s", questionCount, getQuestionTypeName(questionType), prompt);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", systemPrompt));
            messages.add(createMessage("user", userPrompt));
            
            String aiResponse = callAIModel(messages);
            List<Map<String, Object>> questions = parseAIResponse(aiResponse, questionType);
            
            // 如果生成的题目数量不足，补充默认题目
            while (questions.size() < questionCount) {
                questions.add(createFallbackQuestion(prompt, QuestionType.SINGLE_CHOICE));
            }
            
            allQuestions.addAll(questions.subList(0, Math.min(questionCount, questions.size())));
        } catch (Exception e) {
            logger.error("AI生成题目失败", e);
            // 生成默认题目
            for (int i = 0; i < questionCount; i++) {
                allQuestions.add(createFallbackQuestion(prompt, QuestionType.SINGLE_CHOICE));
            }
        }
        
        return allQuestions;
    }
    
    @Override
    public Map<String, Object> generateSingleChoiceQuestion(String content, String difficulty) {
        return generateQuestionFromChunk(content, QuestionType.SINGLE_CHOICE, difficulty);
    }
    
    @Override
    public Map<String, Object> generateMultiChoiceQuestion(String content, String difficulty) {
        return generateQuestionFromChunk(content, QuestionType.MULTI_CHOICE, difficulty);
    }
    
    @Override
    public Map<String, Object> generateJudgeQuestion(String content, String difficulty) {
        return generateQuestionFromChunk(content, QuestionType.JUDGE, difficulty);
    }
    
    @Override
    public List<Map<String, Object>> parseAIResponse(String aiResponse, String questionType) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        try {
            // 清理AI响应
            String cleanedResponse = cleanAIResponse(aiResponse);
            
            // 尝试解析JSON格式
            JsonNode rootNode = JsonUtils.getObjectMapper().readTree(cleanedResponse);
            
            if (rootNode.isArray()) {
                for (JsonNode questionNode : rootNode) {
                    Map<String, Object> question = parseQuestionNode(questionNode, questionType);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            } else if (rootNode.has("questions")) {
                JsonNode questionsNode = rootNode.get("questions");
                for (JsonNode questionNode : questionsNode) {
                    Map<String, Object> question = parseQuestionNode(questionNode, questionType);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            } else {
                // 单个题目
                Map<String, Object> question = parseQuestionNode(rootNode, questionType);
                if (question != null) {
                    questions.add(question);
                }
            }
        } catch (Exception e) {
            logger.warn("JSON解析失败，尝试文本解析", e);
            questions = parseTextResponse(aiResponse, questionType);
        }
        
        return questions;
    }
    
    @Override
    public String buildSystemPrompt(String questionType, String difficulty) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的贷后催收培训题目生成专家。请根据给定的催收相关内容生成高质量的培训考试题目。\n");
        prompt.append("要求：\n");
        prompt.append("1. 题目内容要贴合催收业务实际场景，具有实用性\n");
        prompt.append("2. 题目应涵盖催收话术、法律法规、风险控制、客户心理等方面\n");
        prompt.append("3. 难度等级：").append(difficulty).append("\n");
        prompt.append("4. 【重要】确保每道题目考察的知识点都不相同，避免重复考察同一个知识点\n");
        prompt.append("5. 每道题目应该聚焦于不同的业务场景或知识领域，如：催收技巧、法律条款、风险识别、客户沟通等\n");
        prompt.append("6. 返回JSON格式，包含以下字段：\n");
        
        switch (questionType.toLowerCase()) {
            case "single_choice":
                prompt.append("   - question: 题目内容\n");
                prompt.append("   - options: 选项数组 [\"选项1\", \"选项2\", \"选项3\", \"选项4\"]\n");
                prompt.append("   - correctAnswer: 正确答案 (如 \"A\")\n");
                prompt.append("   - analysis: 答案解析\n");
                break;
            case "multi_choice":
                prompt.append("   - question: 题目内容\n");
                prompt.append("   - options: 选项数组 [\"选项1\", \"选项2\", \"选项3\", \"选项4\"]\n");
                prompt.append("   - correctAnswer: 正确答案 (如 \"A,C\")\n");
                prompt.append("   - analysis: 答案解析\n");
                break;
            case "judge":
                prompt.append("   - question: 题目内容\n");
                prompt.append("   - options: [\"正确\", \"错误\"]\n");
                prompt.append("   - correctAnswer: 正确答案 (\"A\" 或 \"B\")\n");
                prompt.append("   - analysis: 答案解析\n");
                break;
        }
        
        return prompt.toString();
    }
    
    @Override
    public String callAIModel(List<Map<String, String>> messages) {
        try {
            URL url = new URL(AI_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头部
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + AI_API_KEY);
            connection.setDoOutput(true);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            String jsonRequest = JsonUtils.getObjectMapper().writeValueAsString(requestBody);
            
            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            BufferedReader reader;
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            if (responseCode != 200) {
                logger.error("AI API调用失败，响应码：{}，响应内容：{}", responseCode, response.toString());
                throw new RuntimeException("AI API调用失败");
            }
            
            // 解析响应
            JsonNode responseNode = JsonUtils.getObjectMapper().readTree(response.toString());
            return responseNode.path("choices").get(0).path("message").path("content").asText();
            
        } catch (Exception e) {
            logger.error("调用AI模型失败", e);
            throw new RuntimeException("调用AI模型失败", e);
        }
    }
    
    @Override
    public boolean validateQuestionData(Map<String, Object> questionData) {
        return questionData != null && 
               questionData.containsKey("question") && 
               questionData.containsKey("correctAnswer") &&
               !questionData.get("question").toString().trim().isEmpty();
    }
    
    @Override
    public String formatOptions(List<String> options) {
        return String.join("\n", options);
    }
    
    @Override
    public String extractCorrectAnswer(Map<String, Object> questionData, QuestionType questionType) {
        Object answer = questionData.get("correctAnswer");
        if (answer == null) {
            return questionType == QuestionType.JUDGE ? "A" : "A";
        }
        return answer.toString();
    }
    
    // 私有辅助方法
    
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
    
    private String cleanAIResponse(String response) {
        if (response == null) return "{}";
        
        // 移除markdown代码块标记
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        
        // 提取JSON部分
        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group();
        }
        
        return response.trim();
    }
    
    private Map<String, Object> parseQuestionNode(JsonNode questionNode, String questionType) {
        try {
            Map<String, Object> question = new HashMap<>();
            
            question.put("question", questionNode.path("question").asText());
            question.put("correctAnswer", questionNode.path("correctAnswer").asText());
            question.put("analysis", questionNode.path("analysis").asText());
            
            // 处理选项
            JsonNode optionsNode = questionNode.path("options");
            if (optionsNode.isArray()) {
                List<String> options = new ArrayList<>();
                for (JsonNode option : optionsNode) {
                    options.add(option.asText());
                }
                question.put("options", formatOptions(options));
            }
            
            return validateQuestionData(question) ? question : null;
        } catch (Exception e) {
            logger.warn("解析题目节点失败", e);
            return null;
        }
    }
    
    private List<Map<String, Object>> parseTextResponse(String response, String questionType) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        try {
            // 简单的文本解析逻辑
            String[] lines = response.split("\n");
            Map<String, Object> currentQuestion = new HashMap<>();
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("题目：") || line.startsWith("问题：")) {
                    if (!currentQuestion.isEmpty()) {
                        questions.add(currentQuestion);
                        currentQuestion = new HashMap<>();
                    }
                    currentQuestion.put("question", line.substring(3));
                } else if (line.startsWith("答案：")) {
                    currentQuestion.put("correctAnswer", line.substring(3));
                } else if (line.startsWith("解析：")) {
                    currentQuestion.put("analysis", line.substring(3));
                }
            }
            
            if (!currentQuestion.isEmpty()) {
                questions.add(currentQuestion);
            }
        } catch (Exception e) {
            logger.warn("文本解析失败", e);
        }
        
        return questions;
    }
    
    private Map<String, Object> createFallbackQuestion(String content, QuestionType questionType) {
        Map<String, Object> question = new HashMap<>();
        
        switch (questionType) {
            case SINGLE_CHOICE:
                question.put("question", "根据以下内容，请选择正确答案：" + content.substring(0, Math.min(100, content.length())));
                question.put("options", "A. 选项A\nB. 选项B\nC. 选项C\nD. 选项D");
                question.put("correctAnswer", "A");
                question.put("analysis", "这是一道基于内容生成的单选题。");
                break;
            case MULTI_CHOICE:
                question.put("question", "根据以下内容，请选择所有正确答案：" + content.substring(0, Math.min(100, content.length())));
                question.put("options", "A. 选项A\nB. 选项B\nC. 选项C\nD. 选项D");
                question.put("correctAnswer", "A,C");
                question.put("analysis", "这是一道基于内容生成的多选题。");
                break;
            case JUDGE:
                question.put("question", "请判断以下说法是否正确：" + content.substring(0, Math.min(100, content.length())));
                question.put("options", "A. 正确\nB. 错误");
                question.put("correctAnswer", "A");
                question.put("analysis", "这是一道基于内容生成的判断题。");
                break;
            case SHORT_ANSWER:
                question.put("question", "请根据以下内容回答问题：" + content.substring(0, Math.min(100, content.length())));
                question.put("correctAnswer", "参考答案");
                question.put("analysis", "这是一道基于内容生成的简答题。");
                break;
            case FILL_BLANK:
                question.put("question", "请填空：" + content.substring(0, Math.min(100, content.length())) + "______");
                question.put("correctAnswer", "答案");
                question.put("analysis", "这是一道基于内容生成的填空题。");
                break;
        }
        
        return question;
    }
    
    private String getQuestionTypeName(String questionType) {
        switch (questionType.toUpperCase()) {
            case "SINGLE_CHOICE": return "单选题";
            case "MULTI_CHOICE": return "多选题";
            case "SHORT_ANSWER": return "简答题";
            case "FILL_BLANK": return "填空题";
            case "JUDGE": return "判断题";
            default: return "题目";
        }
    }
}