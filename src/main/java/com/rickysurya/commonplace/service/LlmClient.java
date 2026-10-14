package com.rickysurya.commonplace.service;

public interface LlmClient {
    String generate(String systemPrompt, String userPrompt);
}
