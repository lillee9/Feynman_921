package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.AiResult;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Conversation;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.Message;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.ContentDto;
import com.dpsk.dpsk_quiz_sys_java.service.ConversationService;
import com.dpsk.dpsk_quiz_sys_java.service.MessageService;
import com.dpsk.dpsk_quiz_sys_java.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class SeekController {
    private static final Logger logger = LoggerFactory.getLogger(SeekController.class);

    private static final String DONE = "[DONE]";
    private static final Integer timeout = 120; // 增加超时时间到120秒

    private static final String AI_URL = "https://api.deepseek.com/chat/completions";

    private String intactStr = "";

    @Value("${api.password:}")
    private String apiPassword;

    private final MessageService messageService;
    private final ConversationService conversationService;

    public SeekController(MessageService messageService, ConversationService conversationService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
    }


    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping(value = "/stream")
    public void handleSse(@RequestBody StreamRequest request, HttpServletResponse response) {
        System.out.println("收到流式请求 - conversationId: " + request.getConversationId());
        System.out.println("收到流式请求 - messages: " + request.getMessages());
        
        String messages = request.getMessages();
        Integer conversationId = request.getConversationId();
        logger.info("Received messages: {}", messages);
        intactStr = "";
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");

        
        try (PrintWriter pw = response.getWriter()) {
            getAiResult(pw, messages);
            pw.write("data:end\n\n");
            pw.flush();

            //解析json格式的前端返回String
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonArray = mapper.readTree(messages);

            // 获取倒数第二个元素（index = length - 2）
            JsonNode secondLastNode = jsonArray.get(jsonArray.size() - 2);

            // 提取 content 的值
            String content = secondLastNode.get("content").asText();
            //保存到Message表
            //用户消息
            Message userMsg = new Message();
            userMsg.setConversationId(conversationId);
            userMsg.setSenderType(Message.SenderType.USER);
            userMsg.setContent(content);
            messageService.add(userMsg);

            //ai回复
            Message aiMsg = new Message();
            aiMsg.setConversationId(conversationId);
            aiMsg.setSenderType(Message.SenderType.AI);
            aiMsg.setContent(intactStr);
            messageService.add(aiMsg);

        } catch (IOException e) {
            logger.error("IOException occurred while writing response", e);
            throw new RuntimeException("IOException occurred", e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException occurred", e);
            throw new RuntimeException("InterruptedException occurred", e);
        }
    }

    private void getAiResult(PrintWriter pw, String messagesJson) throws InterruptedException {
        // Step 1: 解析前端传来的 JSON 字符串为 List<Map<String, String>>
        List<Map<String, String>> messages = JsonUtils.parseJsonList(messagesJson);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的贷后催收培训小助手。你具备以下技能和职责：\n\n" +
                "【角色定位】\n" +
                "- 贷后催收业务专家和培训导师\n" +
                "- 具备丰富的金融风控和催收实战经验\n" +
                "- 擅长案例分析和情景模拟训练\n\n" +
                "【核心技能】\n" +
                "1. 催收话术指导：提供专业的催收沟通技巧和话术模板\n" +
                "2. 法律法规解读：解答催收相关的法律边界和合规要求\n" +
                "3. 心理分析：分析不同类型客户的心理特征和应对策略\n" +
                "4. 风险评估：指导如何评估客户还款能力和风险等级\n" +
                "5. 案例教学：通过真实案例进行情景化培训\n\n" +
                "【主要职责】\n" +
                "- 为催收人员提供专业培训和指导\n" +
                "- 解答催收过程中遇到的疑难问题\n" +
                "- 分享催收行业最佳实践和经验\n" +
                "- 强调合规催收的重要性\n" +
                "- 提供情绪管理和压力缓解建议\n\n" +
                "【沟通风格】\n" +
                "- 专业严谨但不失亲和力\n" +
                "- 善于用案例和故事进行教学\n" +
                "- 注重实用性和可操作性\n" +
                "- 始终强调合法合规的重要性\n\n" +
                "请用专业、耐心、鼓励的语气与用户交流，帮助他们提升催收技能和职业素养。");
        messages.add(0, systemMessage);

        // Step 2: 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("model", "deepseek-chat");
        params.put("stream", true);
        params.put("messages", messages);

        String jsonParams = JsonUtils.convertObj2Json(params);

        // Step 3: 创建请求
        Request.Builder builder = new Request.Builder().url(AI_URL);
        builder.addHeader("Authorization", "Bearer " + apiPassword);
        builder.addHeader("Accept", "text/event-stream");

        okhttp3.RequestBody body = okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();

        CountDownLatch eventLatch = new CountDownLatch(1);

        // Step 4: 使用 RealEventSource 监听 SSE
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (DONE.equals(data)) {
                    return;
                }
                String content = getContent(data);

                //拼接成完整内容
                intactStr += content;

                pw.write("data:" + JsonUtils.convertObj2Json(new ContentDto(content)) + "\n\n");
                pw.flush();
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                eventLatch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                logger.error("调用接口失败{}", t);
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
}
