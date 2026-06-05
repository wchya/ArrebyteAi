package cn.arrebyte.ai.arrebyteai.openai;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author wch
 * @description
 * @date 2026/6/5 14:07
 */
@SpringBootTest
@Slf4j
public class OpenAiChatSysPromptsTest {
    @Value("classpath:/prompts/sys-msg.st")
    private Resource resource;
    @Autowired
    @Qualifier("openAiChatClient")
    private ChatClient openAiChatClient;

    @BeforeEach
    public void init() throws IOException {
        if (resource.exists()) {
            log.info("resource ->{}", resource.getContentAsString(Charset.defaultCharset()));
        }
    }

    @Test
    void roleTest() {
        UserMessage userMessage = new UserMessage(
                "当前项目的设计怎么样？");
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.resource);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "大胃袋", "voice", "温和"));
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        ChatResponse response = this.openAiChatClient.prompt(prompt).call().chatResponse();
        Assertions.assertNotNull(response);
        assertThat(response.getResults().get(0).getOutput().getText()).contains("大胃袋");
        log.info(
              "回答：-》"+  response.getResult().getOutput().getText()
        );
    }

}
