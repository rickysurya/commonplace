package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.AskResponse;
import com.rickysurya.commonplace.service.AskService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class AskController {

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @GetMapping(value="/ask", produces= MediaType.APPLICATION_JSON_VALUE)
    public AskResponse ask(@RequestParam @NotBlank String q) {
        return askService.ask(q);
    }
}
