package com.rickysurya.commonplace.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private final VectorStore vectorStore;

    public SearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Searches the vector store for chunks semantically similar to the given query,
     * using a default of 5 results.
     *
     * @param query the natural-language query to search for
     * @return up to 5 chunks, ordered by descending similarity score
     */
    public List<Document> search(String query) {
        return search(query, 5);
    }

    /**
     * Searches the vector store for chunks semantically similar to the given query.
     *
     * <p>Uses cosine similarity over the HNSW index. Results are ordered by
     * descending similarity score. If fewer chunks exist than {@code topK},
     * all of them are returned.
     *
     * @param query the natural-language query to search for
     * @param topK  maximum number of chunks to return
     * @return up to {@code topK} chunks, ordered by descending similarity score
     */
    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }
}
