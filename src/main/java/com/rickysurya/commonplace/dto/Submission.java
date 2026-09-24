package com.rickysurya.commonplace.dto;

import jakarta.validation.constraints.NotBlank;

public record Submission(
        @NotBlank(message="text or url is required") String text
) {}
