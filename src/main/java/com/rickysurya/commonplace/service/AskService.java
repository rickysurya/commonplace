package com.rickysurya.commonplace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickysurya.commonplace.client.LlmClient;
import com.rickysurya.commonplace.dto.AskResponse;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import com.rickysurya.commonplace.repository.SearchRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AskService {
    private final LlmClient llmClient;
    private final SearchRepository searchRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public AskService(LlmClient llmClient, SearchRepository searchRepository) {
        this.llmClient = llmClient;
        this.searchRepository = searchRepository;
    }

    private final static String SYSTEM_PROMPT = """
            You are a personal knowledge assistant. Answer the user's question using ONLY the context provided below.
            
             Rules:
             - If the context contains relevant information, use it to answer.
             - If the context does not contain relevant information, respond exactly: "I couldn't find this in your notes."
             - Prefer partial answers over refusing. Only refuse if the context is unrelated to the question.
             - Do not use prior knowledge.
             - Be concise.
             - Do not mention that you were given "context" or "chunks" just answer naturally.
             - When the context directly states the answer, use that wording. Do not rephrase.
             - If the context does not directly state the answer but contains relevant facts, synthesize from those facts.
            
            """;



    private String buildContext(List<Document> contexts) {
        return "Context:\n" + contexts.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
    }

    private String buildUserMessage(String context, String question) {
        return context + "Question: " + question;
    }

    public AskResponse ask(String question) {
        List<Document> docs = searchRepository.search(question);
        if (docs.isEmpty()) {
            return new AskResponse("I couldn't find anything relevant.", List.of());
        }
        String context = buildContext(docs);

        String answer = llmClient.generate(SYSTEM_PROMPT, buildUserMessage(context, question));

        List<String> uniqueSources = docs.stream().map(d -> (String) d.getMetadata().get("source")).filter(Objects::nonNull).distinct().toList();
        return new AskResponse(answer, uniqueSources);
    }

}
