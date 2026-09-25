package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.IngestRequest;
import com.rickysurya.commonplace.service.IngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class IngestionController {

    private final IngestionService ingestionService;
    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    @PostMapping("/submit")
    public ResponseEntity<String> submit(@Valid @RequestBody IngestRequest ingestRequest) {
        try {
            ingestionService.check(ingestRequest.text());
            return ResponseEntity.ok("Submitted");
        } catch (Exception e) {
            log.error("Submit failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

}
