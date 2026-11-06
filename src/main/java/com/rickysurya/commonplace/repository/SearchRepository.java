package com.rickysurya.commonplace.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Repository
public class SearchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchRepository(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    public List<Document> search(String question) {
        String prefixedQuery = "Represent this sentence for searching relevant passages: " + question;
        String vectorString = toPgVector(embeddingModel.embed(prefixedQuery));

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

    private String toPgVector(float[] vector) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (float v : vector) joiner.add(String.valueOf(v));
        return joiner.toString();
    }

    private final RowMapper<Document> documentRowMapper = (rs, rowNum) -> {
        String content = rs.getString("content");
        String metadataJson = rs.getString("metadata");
        Map<String, Object> metadata = Collections.emptyMap();
        if (metadataJson != null) {
            try { metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {}); } catch (Exception ignored) {}
        }
        return new Document(content, metadata);
    };
}
