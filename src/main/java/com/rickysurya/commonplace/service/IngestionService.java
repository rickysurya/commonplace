package com.rickysurya.commonplace.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class IngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;


    public IngestionService(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    /**
     * Ingests text by splitting it into chunks and storing them in the vector store.
     *
     * @param text The raw text to ingest.
     */

    public void ingest(String text, Map<String, Object> metadata) {
        Document doc = new Document(text, metadata);
        List<Document> chunks = tokenTextSplitter.split(doc);
        vectorStore.add(chunks);
    }

    public void ingestText(String text, String source) {
        String src = (source == null || source.isBlank()) ? "manual" : source;
        ingest(text, Map.of("source", src));
    }

    public void ingestFile(MultipartFile file) {
        String text = extractText(file);
        String source = file.getOriginalFilename();
        String src = source != null ? source : "unknown";
        ingest(text, Map.of("source", src));
    }

    private String extractText(MultipartFile file) {
        AutoDetectParser parser = new AutoDetectParser();
        // -1 to prevent truncation
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (InputStream stream = file.getInputStream()) {
            parser.parse(stream, handler, metadata, context);
        } catch (SAXException | IOException | TikaException e) {
            throw new RuntimeException("Failed to extract text from " + file.getOriginalFilename(), e);
        }
        return handler.toString();
    }

}
