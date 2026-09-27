package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.IngestRequest;
import com.rickysurya.commonplace.service.IngestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ingestText(@Valid @RequestBody IngestRequest ingestRequest) {
        try {
            ingestionService.ingest(ingestRequest.text(), Map.of("source", ingestRequest.source()));
            return ResponseEntity.ok(Map.of(
                "status", "ingested",
                "source", ingestRequest.source()));
        } catch (Exception e) {
            log.error("Submit failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping(value="/file", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> ingestFile(@RequestPart("file")MultipartFile file){
        try {
            ingestionService.ingestFile(file);
            return ResponseEntity.ok(Map.of(
                "status", "ingested",
                "source", file.getOriginalFilename()
        ));
        } catch (Exception e) {
            log.error("Submit failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }

    }
//    @PostMapping("/ingest/file")
//    public ResponseEntity<?> ingestFile(@RequestParam("file") MultipartFile file) throws IOException {
//        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
//        ingestionService.ingest(text);
//        return ResponseEntity.ok(Map.of("Ingested", file.getOriginalFilename()));
//    }

}
