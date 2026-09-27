package com.rickysurya.commonplace.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestRequest(
        @NotBlank(message="text required") String text,
        String source
) {

    public IngestRequest{
        if (source == null || source.isBlank()){
            source = "manual";
        }
    }
}


