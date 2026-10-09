package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.config.ChunkingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class IngestionServiceTest {

    VectorStore vectorStore;
    IngestionService service;

    @BeforeEach
    void setup() {
        vectorStore = Mockito.mock(VectorStore.class);
        TokenTextSplitter splitter = new ChunkingConfig().tokenTextSplitter();
        service = new IngestionService(vectorStore, splitter);
    }

    @Test
    void ingestText_usesProvidedSource() {
        service.ingestText("hello world", "ocean-facts.md");

        List<Document> added = captureAdded();
        assertThat(added).isNotEmpty();
        assertThat(added.get(0).getMetadata())
                .containsEntry("source", "ocean-facts.md");
    }

    @Test
    void ingestText_defaultsToManualWhenSourceIsNull() {
        service.ingestText("hello world", null);

        List<Document> added = captureAdded();
        assertThat(added).isNotEmpty();
        assertThat(added.get(0).getMetadata())
                .containsEntry("source", "manual");
    }

    @Test
    void ingestText_defaultsToManualWhenSourceIsBlank() {
        service.ingestText("hello world", "   ");

        List<Document> added = captureAdded();
        assertThat(added).isNotEmpty();
        assertThat(added.get(0).getMetadata())
                .containsEntry("source", "manual");
    }

    @Test
    void ingestFile_usesFilenameAsSource() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "octopus-notes.txt",
                "text/plain",
                "Octopuses have three hearts.".getBytes(StandardCharsets.UTF_8)
        );

        service.ingestFile(file);

        List<Document> added = captureAdded();
        assertThat(added).isNotEmpty();
        assertThat(added.get(0).getMetadata())
                .containsEntry("source", "octopus-notes.txt");
    }

    @Test
    void ingestFile_defaultsToUnknownWhenFilenameIsNull() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "text/plain",
                "some content".getBytes(StandardCharsets.UTF_8)
        );

        service.ingestFile(file);

        List<Document> added = captureAdded();
        assertThat(added).isNotEmpty();
        assertThat(added.get(0).getMetadata())
                .containsEntry("source", "unknown");
    }

    private List<Document> captureAdded() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        return captor.getValue();
    }
}