package cn.arrebyte.ai.arrebyteai;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class ArrebyteAiApplicationTests {

    @Autowired
    @Qualifier("openAiChatClient")
    private ChatClient openAiChatClient;

    @Autowired
    @Qualifier("anthropicChatClient")
    private ChatClient anthropicChatClient;

    @Test
    void contextLoads() {
        log.info("Loaded ChatClient beans: openAiChatClient={}, anthropicChatClient={}",
                openAiChatClient.getClass().getName(),
                anthropicChatClient.getClass().getName());

        assertThat(openAiChatClient).isNotNull();
        assertThat(anthropicChatClient).isNotNull();
    }

    @Test
    void openAiChatClientCanBeDerivedWithDefaultSystemPrompt() {
        ChatClient friendlyOpenAiChatClient = friendlyChatClient(openAiChatClient);

        assertThat(friendlyOpenAiChatClient).isNotNull();
        assertThat(friendlyOpenAiChatClient).isNotSameAs(openAiChatClient);
    }

    @Test
    void anthropicChatClientCanBeDerivedWithDefaultSystemPrompt() {
        ChatClient friendlyAnthropicChatClient = friendlyChatClient(anthropicChatClient);

        assertThat(friendlyAnthropicChatClient).isNotNull();
        assertThat(friendlyAnthropicChatClient).isNotSameAs(anthropicChatClient);
    }

    @Test
    void requestCanUseTemporarySystemPromptWithoutChangingBean() {
        var requestSpec = openAiChatClient.prompt()
                .system(system -> system
                        .text("You are a friendly chat bot that answers question in the voice of a {voice}")
                        .param("voice", "teacher"))
                .user("Explain what Spring AI is. 使用中文回答");
        assertThat(requestSpec).isNotNull();

        String text = Objects.requireNonNull(requestSpec.call().chatResponse()).getResult().getOutput().getText();
        log.info("request can be use temporary system prompt without changing bean: {}", text);
    }

    @Test
    void derivedClientCanUseSystemPromptTemplateParameters() {
        ChatClient friendlyOpenAiChatClient = friendlyChatClient(openAiChatClient);

        var requestSpec = friendlyOpenAiChatClient.prompt()
                .system(system -> system.param("voice", "teacher"))
                .user("Explain what Spring AI is. 使用中文回答");

        assertThat(requestSpec).isNotNull();

        String content = requestSpec.call().content();
        log.info("request can be use temporary system prompt without changing bean: {}", content);
    }

    private ChatClient friendlyChatClient(ChatClient chatClient) {
        return chatClient.mutate()
                .defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
                .build();
    }
}
