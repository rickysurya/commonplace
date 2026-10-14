package com.rickysurya.commonplace.dto;

import java.util.List;

public record AskResponse(
        String answer,
        List<SourceRef> sources
) {
}
