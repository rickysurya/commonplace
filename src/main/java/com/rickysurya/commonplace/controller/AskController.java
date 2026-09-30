package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.AskResponse;
import com.rickysurya.commonplace.service.AskService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/ask")
public class AskController {

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @GetMapping
    public AskResponse ask(@RequestParam @NotBlank String q) {
        return askService.ask(q);
    }
}
