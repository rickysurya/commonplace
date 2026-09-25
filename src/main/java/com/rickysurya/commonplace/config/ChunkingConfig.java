package com.rickysurya.commonplace.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChunkingConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(400) // num_ctx 512
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }
}
