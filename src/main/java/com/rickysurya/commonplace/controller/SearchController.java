package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.service.IngestionService;
import com.rickysurya.commonplace.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam @NotBlank String q) {
        return searchService.search(q);
    }
}
