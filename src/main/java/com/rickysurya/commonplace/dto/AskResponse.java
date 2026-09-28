package com.rickysurya.commonplace.dto;

import java.util.List;

public record AskResponse(
        String Response,
        List<Source> Sources
) {
}
