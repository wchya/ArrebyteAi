package cn.arrebyte.ai.mcpclient.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AI 客户端配置类
 * 将 MCP 客户端获取的工具集成到 ChatClient 中
 */
@Configuration
public class AiClientConfig {
    public static final Logger log = LoggerFactory.getLogger(AiClientConfig.class);



    /**
     * 构建 ChatClient Bean
     * 使用 SyncMcpToolCallbackProvider 将 MCP 服务器的工具包装为 ToolCallbacks
     * 这样 AI 模型就能调用 MCP 服务器提供的工具了[citation:8]
     * <p>
     * MCP 的工作流程：
     * 客户端先连接到 /sse（建立 SSE 连接）
     * 服务器通过 SSE 连接告诉客户端消息端点
     * 客户端再用 /mcp/message 发送具体请求
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 List<McpSyncClient> mcpSyncClients) throws IOException {

        log.info("初始化 ChatClient，发现 {} 个 MCP 客户端", mcpSyncClients.size());

        // 将 MCP 客户端包装为 ToolCallbackProvider
        List<ToolCallback> toolCallbacks = SyncMcpToolCallbackProvider.syncToolCallbacks(mcpSyncClients);
        ChatClient.Builder builder = ChatClient.builder(openAiChatModel);

        // 构建 ChatClient，配置默认系统提示和工具
        return builder
                .defaultSystem(readSystemPromptValue())
                .defaultToolCallbacks(toolCallbacks)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 可选：监听 MCP 工具变更[citation:2][citation:9]
     * 当服务器动态添加/移除工具时，客户端可以收到通知
     */
    @Bean
    public McpSyncClientCustomizer toolsChangeListener() {
        return (name, clientSpec) -> {
            clientSpec.toolsChangeConsumer(changeEvent -> {
                log.info("MCP 工具发生变化: {}", changeEvent);
                // 这里可以更新客户端状态或重新加载工具
            });
        };
    }

    public String readSystemPromptValue() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String path = "prompt/system_prompt.txt";
        return StreamUtils.copyToString(classLoader.getResourceAsStream(path), StandardCharsets.UTF_8);
    }
}
