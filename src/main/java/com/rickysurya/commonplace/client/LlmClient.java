package com.rickysurya.commonplace.client;

public interface LlmClient {
    String generate(String systemPrompt, String userPrompt);
}
