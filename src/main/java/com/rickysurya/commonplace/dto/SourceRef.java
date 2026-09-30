package com.rickysurya.commonplace.dto;

public record SourceRef(
        String text,
        String source,
        double score
) {}
