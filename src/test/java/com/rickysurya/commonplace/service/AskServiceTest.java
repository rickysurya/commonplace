package com.rickysurya.commonplace.service;

import com.rickysurya.commonplace.client.LlmClient;
import com.rickysurya.commonplace.dto.AskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AskServiceTest {

    VectorStore vectorStore;
    LlmClient llmClient;
    AskService service;

    @BeforeEach
    void setup() {
        vectorStore = Mockito.mock(VectorStore.class);
        llmClient = Mockito.mock(LlmClient.class);
        service = new AskService(vectorStore, llmClient);
    }

    @Test
    void ask_shortCircuitsWhenNoDocumentsRetrieved() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        AskResponse response = service.ask("why do octopuses prefer crawling?");

        assertThat(response.answer()).isEqualTo("I couldn't find anything relevant.");
        assertThat(response.sources()).isEmpty();
        verify(llmClient, never()).generate(anyString(), anyString());
    }

    @Test
    void ask_returnsLlmAnswerForRetrievedDocuments() {
        Document doc = new Document("Octopuses have three hearts.",
                Map.of("source", "ocean-facts.md"));
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc));
        when(llmClient.generate(anyString(), anyString()))
                .thenReturn("Octopuses have three hearts.");

        AskResponse response = service.ask("how many hearts?");

        assertThat(response.answer()).isEqualTo("Octopuses have three hearts.");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).text()).isEqualTo("Octopuses have three hearts.");
        assertThat(response.sources().get(0).source()).isEqualTo("ocean-facts.md");
    }

    @Test
    void ask_sendsSystemPromptAndContextToLlm() {
        Document doc = new Document("The US issues the currency it borrows in.",
                Map.of("source", "debt-paper.pdf"));
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc));
        when(llmClient.generate(anyString(), anyString())).thenReturn("answer");

        service.ask("why can't the US default?");

        ArgumentCaptor<String> systemCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        verify(llmClient).generate(systemCaptor.capture(), userCaptor.capture());

        assertThat(systemCaptor.getValue()).contains("personal knowledge assistant");
        assertThat(userCaptor.getValue())
                .contains("The US issues the currency it borrows in.")
                .contains("why can't the US default?");
    }

    @Test
    void ask_multipleDocumentsAreAllIncludedInContext() {
        Document doc1 = new Document("first chunk", Map.of("source", "a.md"));
        Document doc2 = new Document("second chunk", Map.of("source", "b.md"));
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1, doc2));
        when(llmClient.generate(anyString(), anyString())).thenReturn("answer");

        AskResponse response = service.ask("test");

        assertThat(response.sources()).hasSize(2);
        assertThat(response.sources().get(0).source()).isEqualTo("a.md");
        assertThat(response.sources().get(1).source()).isEqualTo("b.md");
    }
}
