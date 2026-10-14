package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.dto.SourceDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SourceServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("pgvector/pgvector:pg16");

    @Autowired SourceService sourceService;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.execute("DELETE FROM vector_store");
    }

    private void insert(String id, String content, String source, int chunkIndex) {
        jdbc.update("""
                INSERT INTO vector_store (id, content, metadata, embedding)
                VALUES (?::uuid, ?, ?::jsonb, array_fill(0, ARRAY[1024])::vector)
                """,
                id,
                content,
                "{\"source\":\"" + source + "\",\"chunk_index\":" + chunkIndex + "}");
    }

    @Test
    void list_groupsBySourceAndCountsChunks() {
        insert("11111111-1111-1111-1111-111111111111", "a", "ocean.md", 0);
        insert("22222222-2222-2222-2222-222222222222", "b", "ocean.md", 1);
        insert("33333333-3333-3333-3333-333333333333", "c", "debt.pdf", 0);

        List<SourceDetail> result = sourceService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo("ocean.md");
        assertThat(result.get(0).chunkCount()).isEqualTo(2);
        assertThat(result.get(1).source()).isEqualTo("debt.pdf");
        assertThat(result.get(1).chunkCount()).isEqualTo(1);
    }

    @Test
    void list_emptyStore_returnsEmptyList() {
        assertThat(sourceService.list()).isEmpty();
    }

    @Test
    void list_excludesRowsWithNullSource() {
        insert("11111111-1111-1111-1111-111111111111", "a", "ocean.md", 0);
        jdbc.update("""
            INSERT INTO vector_store (id, content, metadata, embedding)
            VALUES (?::uuid, ?, ?::jsonb, array_fill(0, ARRAY[1024])::vector)
            """,
                "22222222-2222-2222-2222-222222222222",
                "orphan",
                "{\"chunk_index\":0}");

        List<SourceDetail> result = sourceService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo("ocean.md");
    }

    @Test
    void chunksFor_returnsChunksOrderedByChunkIndex() {
        insert("11111111-1111-1111-1111-111111111111", "second", "ocean.md", 1);
        insert("22222222-2222-2222-2222-222222222222", "first", "ocean.md", 0);

        List<String> chunks = sourceService.chunksFor("ocean.md");

        assertThat(chunks).containsExactly("first", "second");
    }

    @Test
    void chunksFor_unknownSource_returnsEmpty() {
        assertThat(sourceService.chunksFor("nope.md")).isEmpty();
    }
}
