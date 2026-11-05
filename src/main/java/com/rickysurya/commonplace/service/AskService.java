package com.rickysurya.commonplace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rickysurya.commonplace.client.LlmClient;
import com.rickysurya.commonplace.dto.AskResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AskService {
    private final VectorStore vectorStore;
    private final LlmClient llmClient;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public AskService(VectorStore vectorStore, LlmClient llmClient, JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.llmClient = llmClient;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
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

    private String toPgVector(float[] vector) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (float v : vector) {
            joiner.add(String.valueOf(v));
        }
        return joiner.toString();
    }

    // Clean RowMapper to handle the JSON metadata
    private final RowMapper<Document> documentRowMapper = (rs, rowNum) -> {
        String content = rs.getString("content");
        String metadataJson = rs.getString("metadata");
        Map<String, Object> metadata = Collections.emptyMap();

        if (metadataJson != null) {
            try {
                metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});
            } catch (Exception ignored) {
                // Log this in a real app
            }
        }
        return new Document(content, metadata);
    };

    public List<Document> search(String question) {
        // according to mxbai-embed-large the specific prompt is needed for retrieval query
        // source : https://huggingface.co/mixedbread-ai/mxbai-embed-large-v1
        String prefixedQuery = "Represent this sentence for searching relevant passages: " + question;
        String vectorString = toPgVector(embeddingModel.embed(prefixedQuery));

        // BM25 + RRF
        String sql = """
            WITH vector_search AS (
                SELECT id, content, metadata, ROW_NUMBER() OVER (ORDER BY embedding <=> ?::vector) AS rank
                FROM vector_store ORDER BY embedding <=> ?::vector LIMIT 20
            ),
            keyword_search AS (
                SELECT id, content, metadata, ROW_NUMBER() OVER (
                    ORDER BY ts_rank_cd(to_tsvector('english', content), plainto_tsquery('english', ?)) DESC
                ) AS rank
                FROM vector_store 
                WHERE to_tsvector('english', content) @@ plainto_tsquery('english', ?) LIMIT 20
            )
            SELECT 
                COALESCE(v.content, k.content) AS content,
                COALESCE(v.metadata, k.metadata)::text AS metadata,
                (COALESCE(1.0 / (60 + v.rank), 0.0) + COALESCE(1.0 / (60 + k.rank), 0.0)) AS rrf_score
            FROM vector_search v
            FULL OUTER JOIN keyword_search k ON v.id = k.id
            ORDER BY rrf_score DESC LIMIT 5;
            """;

        return jdbcTemplate.query(sql, documentRowMapper,
                vectorString, vectorString, question, question);
    }


    private String buildContext(List<Document> contexts) {
        return "Context:\n" + contexts.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
    }

    private String buildUserMessage(String context, String question) {
        return context + "Question: " + question;
    }

    public AskResponse ask(String question) {
        List<Document> docs = search(question);
        if (docs.isEmpty()) {
            return new AskResponse("I couldn't find anything relevant.", List.of());
        }
        String context = buildContext(docs);

        String answer = llmClient.generate(SYSTEM_PROMPT, buildUserMessage(context, question));

        List<String> uniqueSources = docs.stream().map(d -> (String) d.getMetadata().get("source")).filter(Objects::nonNull).distinct().toList();
        return new AskResponse(answer, uniqueSources);
    }

}
