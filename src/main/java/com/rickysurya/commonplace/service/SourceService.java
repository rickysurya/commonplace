package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.dto.SourceDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SourceService {
    private final JdbcTemplate jdbcTemplate;

    public SourceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SourceDetail> list() {
        return jdbcTemplate.query("""
                          SELECT metadata->>'source' AS source, count(*) as chunk_count
                          FROM vector_store
                          WHERE metadata->>'source' IS NOT NULL
                          GROUP BY metadata->>'source'
                          ORDER BY count(*) DESC
                        """,
                (rs, rowNum) -> new SourceDetail(
                        rs.getInt("chunk_count"),
                        rs.getString("source")
                ));

    }

    public List<String> chunksFor(String source) {
        return jdbcTemplate.queryForList("""
            SELECT content
            FROM vector_store
            WHERE metadata->>'source' = ?
            ORDER BY (metadata->>'chunk_index')::int
            """,
                String.class,
                source);

    }

}