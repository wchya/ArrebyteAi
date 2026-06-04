package cn.arrebyte.ai.arrebyteai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.Objects;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;

    public AiChatController(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {
        this.openAiChatClient = openAiChatClient;
        this.anthropicChatClient = anthropicChatClient;
    }

    @PostMapping("/openai/chat")
    public ChatResponse openAiChat(@RequestBody ChatRequest request) {
        return chat(openAiChatClient, request);
    }
    @PostMapping("/openai/stream/chat")
    public ChatResponse streamOpenAiChat(@RequestBody ChatRequest request) {
        Flux<String> content = openAiChatClient.prompt().user(request.message).stream().content();
        String collect = String.join("", Objects.requireNonNull(content.collectList().block()));
        return new ChatResponse(collect);
    }

    @PostMapping("/anthropic/chat")
    public ChatResponse anthropicChat(@RequestBody ChatRequest request) {
        return chat(anthropicChatClient, request);
    }

    private ChatResponse chat(ChatClient chatClient, ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }

        String content = chatClient.prompt()
                .user(request.message())
                .call()
                .content();

        return new ChatResponse(content);
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String content) {
    }
}
