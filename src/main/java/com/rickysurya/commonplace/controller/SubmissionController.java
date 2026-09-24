package com.rickysurya.commonplace.controller;

import com.rickysurya.commonplace.dto.Submission;
import com.rickysurya.commonplace.service.SubmissionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    @Autowired
    SubmissionService submissionService;

    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    @PostMapping("/submit")
    public ResponseEntity<String> submit(@Valid @RequestBody Submission submission) {
        if (submission.text() == null || submission.text().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            submissionService.check(submission.text());
            return ResponseEntity.ok("Submitted");
        } catch (Exception e) {
            log.error("Submit failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }

    }
}
