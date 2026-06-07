package cn.arrebyte.ai.arrebyteai.openai;

import cn.arrebyte.ai.arrebyteai.ArrebyteAiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

/**
 * @author wch
 * @description 多模态
 * @date 2026/6/6 10:28
 */
@SpringBootTest(classes = ArrebyteAiApplication.class)
public class MultiModalTest {

    @Autowired
    @Qualifier("openAiChatClient")
    private ChatClient openAiChatClient;

    @Test
    public void multiModalTest() {
        var classPathResource = new ClassPathResource("prompts/att.png");
        var userMessage = UserMessage.builder().media(new Media(MimeTypeUtils.IMAGE_PNG, classPathResource))
                .text("解释这张照片").build();
        String content = openAiChatClient.prompt().messages(userMessage).call().content();
        System.out.println(content);
    }

    @Test
    public void multiModalTest1() {
        String content = openAiChatClient.prompt()
                .user(u -> u.text("解释这张照片")
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("prompts/att.png")))
                .call()
                .content();
        System.out.println(content);
    }
}
