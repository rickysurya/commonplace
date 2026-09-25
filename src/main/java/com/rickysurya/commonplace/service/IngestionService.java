package com.rickysurya.commonplace.service;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }

    /**
     * Ingests text by splitting it into chunks and storing them in the vector store.
     *
     * @param text The raw text to ingest.
     * @return The number of chunks created.
     */

    // TODO add metadata e.g. source, date, title to the document
    public int ingest(String text) {
        Document doc = new Document(text);
        List<Document> chunks = tokenTextSplitter.split(doc);
        vectorStore.add(chunks);
        return chunks.size();
    }

    /**
     * Accepts either raw text or a URL and routes it to the appropriate ingestion pipeline.
     *
     * <p>If the input is valid raw text, it is chunked and embedded immediately.
     * If the input is a valid URL, fetching is not yet implemented — this method
     * currently does nothing for URLs.
     *
     * @param input raw text to ingest, or a URL whose content should eventually be ingested
     */

    public void check(String input) {
        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(input)) {
            ingest(input);
        } else {
            // TODO fetch url content before ingesting
        }
    }
}
