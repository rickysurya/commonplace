package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.SourceDetail;
import com.rickysurya.commonplace.service.SourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {
    private final SourceService sourceService;
    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    public List<SourceDetail> list(){
        return sourceService.list();
    }

    @GetMapping("/{source}/chunks")
    public List<String> chunks(@PathVariable String source) {
        return sourceService.chunksFor(source);
    }

}
