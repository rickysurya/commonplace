package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.SourceDetail;
import com.rickysurya.commonplace.service.SourceService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {
    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    public List<SourceDetail> list() {
        return sourceService.list();
    }

    @GetMapping("/chunks")
    public List<String> chunks(@RequestParam @NotBlank String source) {
        return sourceService.chunksFor(source);
    }
}
