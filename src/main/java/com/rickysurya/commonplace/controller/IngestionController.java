package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.IngestRequest;
import com.rickysurya.commonplace.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ingestText(@Valid @RequestBody IngestRequest ingestRequest) {
        ingestionService.ingestText(ingestRequest.text(), ingestRequest.source());
        return ResponseEntity.ok(Map.of("status", "ingested"));
   }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> ingestFile(@RequestPart("file") MultipartFile file) {
        ingestionService.ingestFile(file);
        return ResponseEntity.ok(Map.of("status", "ingested"));
    }
}
