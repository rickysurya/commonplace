package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.common.Fetch;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class IngestionService {

    private final Fetch fetch;
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    public IngestionService(Fetch fetch, VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.fetch = fetch;
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    /**
     * Ingests text by splitting it into chunks and storing them in the vector store.
     *
     * @param text The raw text to ingest.
     */

    // TODO add metadata e.g. source, date, title to the document
    public void ingest(String text) {
        Document doc = new Document(text);
        List<Document> chunks = tokenTextSplitter.split(doc);
        vectorStore.add(chunks);
    }

    //TODO once fetch url is finished
    public void ingestUrl(String url) {
        try {
            ingest(fetch.fetchUrl(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
