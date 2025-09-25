package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.AiResult;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ContentDto;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ResponseRecord;
import com.dpsk.dpsk_quiz_sys_java.service.BaiduOcrService;
import com.dpsk.dpsk_quiz_sys_java.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@CrossOrigin()
@RequestMapping("/answer")
public class AnswerController {
    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    private static final String DONE = "[DONE]";
    private static final Integer timeout = 120;
    private static final String AI_URL = "https://api.deepseek.com/chat/completions";

    @Value("${api.password:}")
    private String apiPassword;

    @Autowired
    private BaiduOcrService ocrService;

    @PostMapping("/process")
    public void processImageAndAnswer(
            @RequestParam("image") MultipartFile image,
            HttpServletResponse response) throws IOException {

        // 1. 处理OCR识别
        String ocrText = processOcr(image);
        if (ocrText == null || ocrText.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("OCR识别失败或未识别到文本");
            return;
        }

        // 2. 设置SSE响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        // 3. 记录用户提问（OCR识别内容）
        ResponseRecord userQuery = new ResponseRecord(
                UUID.randomUUID().toString(),
                ResponseRecord.TYPE_USER_QUERY,
                System.currentTimeMillis(),
                ocrText,
                ocrText
        );
        logUserQuery(userQuery);

        // 4. 调用AI接口获取答案
        try (PrintWriter pw = response.getWriter()) {
            getAiAnswer(pw, ocrText);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            logger.error("处理过程中发生错误", e);
            throw new RuntimeException("处理过程中发生错误", e);
        }
    }

    private String processOcr(MultipartFile image) {
        try {
            // 调用OCR服务识别图片内容
            String ocrResult = ocrService.recognizeImg(image);

            // 对OCR结果进行简单处理：去除多余空格、换行等
            return integrateOcrResults(ocrResult);
        } catch (Exception e) {
            logger.error("OCR处理失败", e);
            return null;
        }
    }
    private String integrateOcrResults(String ocrJson) {
        try {
            // 解析JSON
            JsonNode rootNode = JsonUtils.getObjectMapper().readTree(ocrJson);
            JsonNode wordsResults = rootNode.path("words_result");

            // 拼接所有识别内容
            StringBuilder integratedText = new StringBuilder();
            for (JsonNode item : wordsResults) {
                integratedText.append(item.path("words").asText())
                        .append(" ");  // 用空格分隔不同识别块
            }

            // 返回整合后的字符串（去除首尾空格）
            return integratedText.toString().trim();
        } catch (IOException e) {
            logger.error("OCR结果解析失败", e);
            return ocrJson; // 解析失败时返回原始JSON
        }
    }

    private void getAiAnswer(PrintWriter pw, String question) throws InterruptedException {
        // 构造对话消息
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的贷后催收培训助手，擅长分析和解读催收相关的文档、案例和问题。你能够：\n" +
                "1. 解析催收相关的文档内容，如催收函、还款协议、法律文书等\n" +
                "2. 分析催收案例，提供专业的处理建议和话术指导\n" +
                "3. 解答催收过程中的法律法规问题\n" +
                "4. 识别文档中的关键信息，如逾期金额、还款期限、客户信息等\n" +
                "5. 提供合规的催收建议和风险提示\n\n" +
                "请用专业、准确的语言分析用户提供的内容，并给出实用的催收指导建议。");
        messages.add(systemMessage);

        // 用户问题（OCR识别内容）
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "请解析以下内容,如果有问题，请回答相关问题:\n" + question);
        messages.add(userMessage);

        // 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("model", "deepseek-chat");
        params.put("stream", true);
        params.put("messages", messages);

        String jsonParams = JsonUtils.convertObj2Json(params);

        // 创建请求
        Request.Builder builder = new Request.Builder().url(AI_URL);
        builder.addHeader("Authorization", "Bearer " + apiPassword);
        builder.addHeader("Accept", "text/event-stream");

        RequestBody body = okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();
        //配置HTTP客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
        //处理SSE事件流
        CountDownLatch eventLatch = new CountDownLatch(1);
        List<ResponseRecord> aiResponses = new ArrayList<>();

        // 使用 RealEventSource 监听 SSE
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (DONE.equals(data)) {
                    return;
                }
                String content = getContent(data);

                // 记录AI响应
                aiResponses.add(new ResponseRecord(
                        id,
                        ResponseRecord.TYPE_AI_RESPONSE,
                        System.currentTimeMillis(),
                        content,
                        data
                ));

                pw.write("data:" + JsonUtils.convertObj2Json(new ContentDto(content)) + "\n\n");
                pw.flush();
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                logAiResponse(aiResponses);
                eventLatch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                logger.error("调用Deepseek API失败", t);
                if (eventLatch != null) {
                    eventLatch.countDown();
                }
            }
        });

        realEventSource.connect(client);
        eventLatch.await();
    }

    private static String getContent(String data) {
        AiResult aiResult = JsonUtils.convertJson2Obj(data, AiResult.class);
        return aiResult.getChoices().get(0).getDelta().getContent();
    }

    private void logUserQuery(ResponseRecord userQuery) {
        String cleanContent = userQuery.getContent()
                .replaceAll("\\s+", " ")  // 合并多个空格
                .trim();

        logger.info("\n===== 用户提问(OCR识别内容) =====\n" +
                        "ID: {}\n" +
                        "识别内容: {}\n",
                userQuery.getId(),
                cleanContent);
    }

    private void logAiResponse(List<ResponseRecord> aiResponses) {
        String fullContent = aiResponses.stream()
                .map(ResponseRecord::getContent)
                .collect(Collectors.joining());

        logger.info("\n===== AI完整响应 =====\n{}\n", fullContent);
    }
}
