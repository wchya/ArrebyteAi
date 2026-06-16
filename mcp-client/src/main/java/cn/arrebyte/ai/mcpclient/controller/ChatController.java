package cn.arrebyte.ai.mcpclient.controller;


import cn.arrebyte.ai.mcpclient.entity.ChatRequest;
import cn.arrebyte.ai.mcpclient.entity.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天控制器
 * 提供 REST API 供前端调用
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    public static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;

    /**
     * 处理用户消息，使用 MCP 工具增强回答
     */
    @PostMapping("/message")
    public ChatResponse processMessage(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();
        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : "未绑定PDF会话";
        log.info("收到用户消息: {}", userMessage);

        // 构建包含上下文的提示词
        String enhancedPrompt = String.format(
                "当前会话ID是：%s\n" +
                        "用户问题：%s\n\n" +
                        "注意：如果需要查询PDF内容，请使用queryPdf工具，并将会话ID作为chatId参数传入；如果当前没有绑定PDF会话，则不要调用 PDF 工具。",
                sessionId, userMessage
        );


        try {
            // 调用 ChatClient，自动使用 MCP 工具
            String response = chatClient.prompt()
                    .user(enhancedPrompt)
                    .call()
                    .content();

            log.info("AI 回复: {}", response);

            return ChatResponse.builder()
                    .success(true)
                    .message(response)
                    .build();

        } catch (Exception e) {
            log.error("处理消息失败", e);
            return ChatResponse.builder()
                    .success(false)
                    .message("处理失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 查询可用工具（调试用）
     */
    @GetMapping("/tools")
    public Object listAvailableTools() {
        return chatClient.prompt()
                .user("当前有哪些可用的工具？请列出工具名称和功能。")
                .call()
                .content();
    }
}
