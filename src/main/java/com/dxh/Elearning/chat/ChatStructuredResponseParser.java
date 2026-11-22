package com.dxh.Elearning.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatStructuredResponseParser {
    private final ObjectMapper objectMapper;

    public Optional<ChatAssistantResult> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return Optional.empty();
        }

        int start = rawResponse.indexOf('{');
        int end = rawResponse.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(
                    rawResponse.substring(start, end + 1),
                    ChatAssistantResult.class
            ));
        } catch (JsonProcessingException exception) {
            log.warn("Chat model returned invalid structured JSON: {}", exception.getOriginalMessage());
            return Optional.empty();
        }
    }
}
