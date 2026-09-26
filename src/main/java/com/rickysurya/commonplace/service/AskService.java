package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.dto.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AskService {
    private final VectorStore vectorStore;
    private ChatClient chatClient;

    public AskService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    private final static String SYSTEM_PROMPT = """
            You are a personal knowledge assistant. Answer the user's question using ONLY the context provided below.
            
             Rules:
             - If the answer is not in the context, respond exactly: "I couldn't find this in your notes."
             - Do not use prior knowledge.
             - Be concise.
             - Do not mention that you were given "context" or "chunks" — just answer naturally.
            
            """;


    public List<Document> search(String question) {
        // according to mxbai-embed-large the specific prompt is needed for retrieval query
        // source : https://huggingface.co/mixedbread-ai/mxbai-embed-large-v1
        String prefixedQuery = "Represent this sentence for searching relevant passages: " + question;
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(prefixedQuery)
                        .topK(5)
                        .build()
        );
    }

    private String buildContext(List<Document> contexts) {
        return "Context:\n" + contexts.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String buildUserMessage(String context, String question) {
        return context + "Question: " + question;
    }

    public AskResponse ask(String question) {
        List<Document> contexts = search(question);
        if (contexts.isEmpty()) {
            return new AskResponse("I couldn't find anything relevant.",List.of());
        }
        String context = buildContext(contexts);

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(buildUserMessage(context, question))
                .call()
                .content();
        return new AskResponse(answer, List.of(context));
    }

}
