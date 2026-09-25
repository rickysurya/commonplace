package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.IngestRequest;
import com.rickysurya.commonplace.service.IngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IngestionController {

    private final IngestionService ingestionService;
    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@Valid @RequestBody IngestRequest ingestRequest) {
        try {
            ingestionService.check(ingestRequest.text());
            return ResponseEntity.ok("Ingested");
        } catch (Exception e) {
            log.error("Submit failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    // naive assumption .txt for testing purpose
    @PostMapping("/ingest/file")
    public ResponseEntity<?> ingestFile(@RequestParam("file") MultipartFile file) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        ingestionService.ingest(text);
        return ResponseEntity.ok(Map.of("Ingested", file.getOriginalFilename()));
    }

}
