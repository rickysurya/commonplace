package com.rickysurya.commonplace.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaLlmClient implements LlmClient {
    private final ChatClient chatClient;

    public OllamaLlmClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
