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




    public void check(String submission) {
        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(submission)) {
            ingest(submission);
        } else {
            // TODO fetch url content before ingesting
        }
    }
}
